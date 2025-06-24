package ar.edu.unlam.scaffoldingandroid3.ui.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingStatus
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingResult
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.StartTrackingUseCase
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.StopTrackingUseCase
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.UpdateTrackingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel SIMPLIFICADO para tracking
 * Solo observa datos del TrackingService via UseCase
 */
@HiltViewModel
class TrackingViewModel
    @Inject
    constructor(
        private val startTrackingUseCase: StartTrackingUseCase,
        private val stopTrackingUseCase: StopTrackingUseCase,
        private val updateTrackingUseCase: UpdateTrackingUseCase,
        private val metricsCalculator: ar.edu.unlam.scaffoldingandroid3.data.sensor.MetricsCalculator,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(TrackingUiState())
        val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

        private val _metrics = MutableStateFlow(TrackingMetrics())
        val metrics: StateFlow<TrackingMetrics> = _metrics.asStateFlow()

        private val _trackingStatus = MutableStateFlow(TrackingStatus.COMPLETED)
        val trackingStatus: StateFlow<TrackingStatus> = _trackingStatus.asStateFlow()

        private val _detailedStats = MutableStateFlow<Map<String, Any>>(emptyMap())
        val detailedStats: StateFlow<Map<String, Any>> = _detailedStats.asStateFlow()

        init {
            observeTrackingData()
        }

        private fun observeTrackingData() {
            // Observar estado de tracking desde el servicio
            viewModelScope.launch {
                updateTrackingUseCase.getTrackingStatus()
                    .catch { handleError("Error al observar estado: ${it.message}") }
                    .collect { status ->
                        _trackingStatus.value = status
                        updateUiStateForTrackingStatus(status)
                    }
            }

            // Observar métricas en tiempo real desde el servicio
            viewModelScope.launch {
                updateTrackingUseCase.getCurrentMetrics()
                    .catch { /* Métricas no críticas para UI */ }
                    .collect { metrics ->
                        _metrics.value = metrics
                        updateDetailedStats()
                        updateUiFromMetrics(metrics)
                    }
            }
        }

        private fun updateUiStateForTrackingStatus(status: TrackingStatus) {
            val current = _uiState.value
            val newScreenState =
                when (status) {
                    TrackingStatus.COMPLETED -> TrackingScreenState.PREPARATION
                    TrackingStatus.ACTIVE, TrackingStatus.PAUSED -> {
                        if (current.isStatsExpanded) {
                            TrackingScreenState.EXPANDED_STATS
                        } else {
                            TrackingScreenState.RECORDING
                        }
                    }
                }

            _uiState.value =
                current.copy(
                    screenState = newScreenState,
                    isTracking = status == TrackingStatus.ACTIVE,
                    isPaused = status == TrackingStatus.PAUSED,
                    canPause = status == TrackingStatus.ACTIVE,
                    canResume = status == TrackingStatus.PAUSED,
                    canStop = status != TrackingStatus.COMPLETED,
                )
        }

        private suspend fun updateDetailedStats() {
            updateTrackingUseCase.getDetailedStats()?.let { stats ->
                _detailedStats.value = stats
            }
        }

        private fun updateUiFromMetrics(metrics: TrackingMetrics) {
            // Obtener datos desde DetailedStats simplificado
            viewModelScope.launch {
                try {
                    val detailedStats = updateTrackingUseCase.getDetailedStats()
                    detailedStats?.let { stats ->
                        // Extraer valores simples
                        val elapsedTime = stats["elapsedTimeFormatted"] as? String ?: "00:00:00"
                        val steps = (stats["totalSteps"] as? Int) ?: 0
                        val routePoints = createRoutePointsFromMetrics(metrics)
                        val currentLocation = createCurrentLocationFromMetrics(metrics)

                        _uiState.value =
                            _uiState.value.copy(
                                elapsedTime = elapsedTime,
                                stepCount = steps,
                                currentAltitude = metrics.currentElevation,
                                routePoints = routePoints,
                                currentLocation = currentLocation,
                                photoCount = _uiState.value.capturedPhotos.size,
                            )
                    }
                } catch (e: Exception) {
                    // Error no crítico
                }
            }
        }

        private fun createRoutePointsFromMetrics(metrics: TrackingMetrics): List<com.google.android.gms.maps.model.LatLng> {
            // Obtener TODOS los puntos de la ruta desde MetricsCalculator para pintar el camino completo
            return try {
                val allRoutePoints = metricsCalculator.getCurrentRoutePoints()
                allRoutePoints.map { point ->
                    com.google.android.gms.maps.model.LatLng(point.latitude, point.longitude)
                }
            } catch (e: Exception) {
                // Fallback: usar solo la ubicación actual si hay error
                metrics.lastLocation?.let { location ->
                    listOf(com.google.android.gms.maps.model.LatLng(location.latitude, location.longitude))
                } ?: emptyList()
            }
        }

        private fun createCurrentLocationFromMetrics(metrics: TrackingMetrics): com.google.android.gms.maps.model.LatLng? {
            return metrics.lastLocation?.let { location ->
                com.google.android.gms.maps.model.LatLng(location.latitude, location.longitude)
            }
        }

        fun startTracking(routeName: String) {
            if (routeName.trim().isEmpty()) {
                handleError("El nombre de la ruta no puede estar vacío")
                return
            }

            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                startTrackingUseCase.execute(routeName.trim())
                    .onSuccess {
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                routeName = routeName.trim(),
                            )
                    }
                    .onFailure { error ->
                        val errorMessage = when {
                            error.message?.contains("Permisos de ubicación") == true -> 
                                "⚠️ Permisos de ubicación requeridos\n\nOtorga el permiso 'Mientras usas la app' y mantén la app abierta durante el tracking"
                            error.message?.contains("segundo plano") == true -> 
                                "⚠️ Para tracking continuo, ve a:\nConfiguración → Apps → ScaffoldingAndroid3 → Permisos → Ubicación → 'Permitir todo el tiempo'"
                            error.message?.contains("GPS") == true -> 
                                "⚠️ GPS deshabilitado\n\nActiva la ubicación en Configuración del dispositivo"
                            error.message?.contains("notificación") == true -> 
                                "⚠️ Permisos de notificación requeridos\n\nVe a: Configuración → Apps → ScaffoldingAndroid3 → Permisos → Notificaciones → Permitir"
                            else -> error.message ?: "Error al iniciar tracking"
                        }
                        
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                error = errorMessage,
                            )
                    }
            }
        }

        fun pauseTracking() {
            viewModelScope.launch {
                updateTrackingUseCase.pauseTracking()
                    .onFailure { error ->
                        handleError(error.message ?: "Error al pausar tracking")
                    }
            }
        }

        fun resumeTracking() {
            viewModelScope.launch {
                updateTrackingUseCase.resumeTracking()
                    .onFailure { error ->
                        handleError(error.message ?: "Error al reanudar tracking")
                    }
            }
        }

        fun stopTracking(onCompleted: (TrackingResult) -> Unit) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)

                stopTrackingUseCase.execute()
                    .onSuccess { session ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        val trackingResult = createTrackingResult(session)
                        onCompleted(trackingResult)
                    }
                    .onFailure { error ->
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                error = error.message ?: "Error al detener tracking",
                            )
                    }
            }
        }

        fun toggleStatsExpansion() {
            val current = _uiState.value
            val newExpanded = !current.isStatsExpanded

            val newScreenState =
                if (newExpanded && current.canStop) {
                    TrackingScreenState.EXPANDED_STATS
                } else if (current.canStop) {
                    TrackingScreenState.RECORDING
                } else {
                    TrackingScreenState.PREPARATION
                }

            _uiState.value =
                current.copy(
                    isStatsExpanded = newExpanded,
                    screenState = newScreenState,
                )
        }

        fun updateRouteName(routeName: String) {
            _uiState.value = _uiState.value.copy(routeName = routeName)
        }

        fun clearError() {
            _uiState.value = _uiState.value.copy(error = null)
        }

        fun checkTrackingAvailability(callback: (Boolean, List<String>) -> Unit) {
            viewModelScope.launch {
                val canStart = startTrackingUseCase.canStartTracking()
                val blockingReasons =
                    if (!canStart) {
                        startTrackingUseCase.getBlockingReasons()
                    } else {
                        emptyList()
                    }
                callback(canStart, blockingReasons)
            }
        }

        fun capturePhoto() {
            // TEMPORALMENTE DESHABILITADO para debug de crash
            // La funcionalidad de cámara se implementará después de solucionar el crash
            handleError("Funcionalidad de cámara temporalmente deshabilitada")
            
            // Simular captura de foto para testing
            val fakePhoto = ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingPhoto(
                uri = "fake://photo_${System.currentTimeMillis()}",
                orderInRoute = _uiState.value.capturedPhotos.size
            )
            
            val currentPhotos = _uiState.value.capturedPhotos.toMutableList()
            currentPhotos.add(fakePhoto)
            _uiState.value = _uiState.value.copy(
                capturedPhotos = currentPhotos,
                photoCount = currentPhotos.size
            )
        }
        
        private fun createTrackingResult(session: TrackingSession): TrackingResult {
            val totalDuration = session.endTime - session.startTime
            val tiempoTotal = formatTime(totalDuration)
            
            // Obtener tiempo en movimiento real y estadísticas adicionales
            viewModelScope.launch {
                val detailedStats = updateTrackingUseCase.getDetailedStats()
                val movementTime = detailedStats?.get("movementTime") as? Long ?: totalDuration
                val tiempoEnMovimiento = formatTime(movementTime)
                val minAltitude = detailedStats?.get("minAltitude") as? Double ?: session.metrics.currentElevation
                val maxAltitude = detailedStats?.get("maxAltitude") as? Double ?: session.metrics.currentElevation
            }
            
            // Por ahora, crear con valores actuales (mejora futura: hacer suspendible)
            val movementDuration = session.metrics.currentDuration // Tiempo en movimiento desde MetricsCalculator
            val tiempoEnMovimiento = formatTime(movementDuration)
            
            return TrackingResult(
                tiempoTotal = tiempoTotal,
                tiempoEnMovimiento = tiempoEnMovimiento,
                distanciaTotal = session.metrics.currentDistance,
                pasosTotales = _uiState.value.stepCount,
                velocidadMedia = session.metrics.averageSpeed, // Ahora calculado correctamente
                velocidadMaxima = session.metrics.maxSpeed,
                altitudMinima = metricsCalculator.getMinAltitude(),
                altitudMaxima = metricsCalculator.getMaxAltitude(),
                rutaCompleta = session.routePoint,
                fotosCapturadas = _uiState.value.capturedPhotos,
                nombreRecorrido = "",
                fechaCreacion = System.currentTimeMillis()
            )
        }
        
        private fun formatTime(millis: Long): String {
            val seconds = (millis / 1000) % 60
            val minutes = (millis / (1000 * 60)) % 60
            val hours = millis / (1000 * 60 * 60)
            return "%02d:%02d:%02d".format(hours, minutes, seconds)
        }

        fun showDiscardDialog() {
            _uiState.value = _uiState.value.copy(showDiscardDialog = true)
        }
        
        fun hideDiscardDialog() {
            _uiState.value = _uiState.value.copy(showDiscardDialog = false)
        }
        
        fun discardTracking() {
            // Detener tracking y limpiar estado
            viewModelScope.launch {
                stopTrackingUseCase.execute()
                _uiState.value = TrackingUiState() // Reset a estado inicial
            }
        }

        private fun handleError(message: String) {
            _uiState.value =
                _uiState.value.copy(
                    error = message,
                    isLoading = false,
                )
        }
    }
