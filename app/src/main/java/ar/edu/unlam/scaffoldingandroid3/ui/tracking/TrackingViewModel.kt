package ar.edu.unlam.scaffoldingandroid3.ui.tracking

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingPhoto
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingResult
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingStatus
import ar.edu.unlam.scaffoldingandroid3.domain.repository.SensorRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.TrackingSessionRepository
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.StartTrackingUseCase
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.StopTrackingUseCase
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.UpdateTrackingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
        private val trackingSessionRepository: TrackingSessionRepository,
        private val sensorRepository: SensorRepository,
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
            // Acceso directo a Repository - NO pass-through
            viewModelScope.launch {
                trackingSessionRepository.getTrackingStatus()
                    .catch { handleError("Error al observar estado: ${it.message}") }
                    .collect { status ->
                        _trackingStatus.value = status
                        updateUiStateForTrackingStatus(status)
                    }
            }

            // Acceso directo a Repository - NO pass-through
            viewModelScope.launch {
                trackingSessionRepository.getCurrentMetrics()
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
            // Obtener datos directamente del repository y formatear para UI
            try {
                val session = trackingSessionRepository.getCurrentTrackingSession()
                if (session != null) {
                    val metrics = session.metrics
                    val currentTime = metrics.currentDuration

                    val stats =
                        mapOf(
                            "routeName" to session.routeName,
                            "status" to session.status.name,
                            "elapsedTime" to currentTime,
                            "elapsedTimeFormatted" to formatTime(currentTime),
                            "totalSteps" to metrics.totalSteps,
                            "currentSpeed" to metrics.currentSpeed,
                            "averageSpeed" to metrics.averageSpeed,
                            "maxSpeed" to metrics.maxSpeed,
                            "distance" to metrics.currentDistance,
                            "currentElevation" to metrics.currentElevation,
                        )
                    _detailedStats.value = stats
                }
            } catch (e: Exception) {
                // Error no crítico
            }
        }

        private fun updateUiFromMetrics(metrics: TrackingMetrics) {
            viewModelScope.launch {
                try {
                    updateDetailedStats() // Para stats avanzadas
                    val detailedStats = _detailedStats.value

                    // Extraer tiempo formateado de detailedStats
                    val elapsedTime = detailedStats["elapsedTimeFormatted"] as? String ?: "00:00:00"

                    // Obtener pasos DIRECTAMENTE de métricas
                    val steps = metrics.totalSteps

                    val routePoints = createRoutePointsFromMetrics(metrics)
                    val currentLocation = createCurrentLocationFromMetrics(metrics)

                    _uiState.value =
                        _uiState.value.copy(
                            elapsedTime = elapsedTime,
                            // Usa directamente metrics.totalSteps
                            stepCount = steps,
                            currentAltitude = metrics.currentElevation,
                            routePoints = routePoints,
                            currentLocation = currentLocation,
                            photoCount = _uiState.value.capturedPhotos.size,
                        )
                } catch (e: Exception) {
                    // Error updating UI from metrics
                }
            }
        }

        private suspend fun createRoutePointsFromMetrics(metrics: TrackingMetrics): List<com.google.android.gms.maps.model.LatLng> {
            // Obtener TODOS los puntos de la ruta para dibujar el camino completo
            return try {
                val routePoints = trackingSessionRepository.getCurrentRoutePoints()
                routePoints.map { point ->
                    com.google.android.gms.maps.model.LatLng(point.latitude, point.longitude)
                }
            } catch (e: Exception) {
                // Fallback: usar solo la última ubicación si hay error
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
                        val errorMessage =
                            when {
                                error.message?.contains("Permisos de ubicación") == true ->
                                    "⚠️ Permisos de ubicación requeridos\n\n" +
                                        "Otorga el permiso 'Mientras usas la app' y mantén la app abierta durante el tracking"
                                error.message?.contains("segundo plano") == true ->
                                    "⚠️ Para tracking continuo, ve a:\n" +
                                        "Configuración → Apps → ScaffoldingAndroid3 → Permisos → Ubicación → 'Permitir todo el tiempo'"
                                error.message?.contains("GPS") == true ->
                                    "⚠️ GPS deshabilitado\n\nActiva la ubicación en Configuración del dispositivo"
                                error.message?.contains("notificación") == true ->
                                    "⚠️ Permisos de notificación requeridos\n\n" +
                                        "Ve a: Configuración → Apps → ScaffoldingAndroid3 → Permisos → Notificaciones → Permitir"
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

        fun createImageFile(context: Context): File {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = File(context.getExternalFilesDir(null), "tracking_photos")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
        }

        fun onPhotoTaken(uri: Uri) {
            val photo = TrackingPhoto(
                uri = uri.toString(),
                orderInRoute = _uiState.value.capturedPhotos.size
            )

            val currentPhotos = _uiState.value.capturedPhotos.toMutableList()
            currentPhotos.add(photo)

            _uiState.value = _uiState.value.copy(
                capturedPhotos = currentPhotos,
                photoCount = currentPhotos.size,
                lastPhotoUri = uri.toString()
            )
        }

        fun capturePhoto() {
            // TEMPORALMENTE DESHABILITADO para debug de crash
            // La funcionalidad de cámara se implementará después de solucionar el crash
            handleError("Funcionalidad de cámara temporalmente deshabilitada")

            // Simular captura de foto para testing
            val fakePhoto =
                ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingPhoto(
                    uri = "fake://photo_${System.currentTimeMillis()}",
                    orderInRoute = _uiState.value.capturedPhotos.size,
                )

            val currentPhotos = _uiState.value.capturedPhotos.toMutableList()
            currentPhotos.add(fakePhoto)
            _uiState.value =
                _uiState.value.copy(
                    capturedPhotos = currentPhotos,
                    photoCount = currentPhotos.size,
                )
        }

        private fun createTrackingResult(session: TrackingSession): TrackingResult {
            // Usar solo tiempo de movimiento (sin pausas)
            val duracion = formatTime(session.metrics.currentDuration)

            return TrackingResult(
                duracion = duracion,
                distanciaTotal = session.metrics.currentDistance,
                pasosTotales = session.metrics.totalSteps,
                // Ahora calculado correctamente
                velocidadMedia = session.metrics.averageSpeed,
                velocidadMaxima = session.metrics.maxSpeed,
                // Simplificado por ahora
                altitudMinima = session.metrics.currentElevation,
                // Simplificado por ahora
                altitudMaxima = session.metrics.currentElevation + 50,
                rutaCompleta = session.routePoint,
                fotosCapturadas = _uiState.value.capturedPhotos,
                nombreRecorrido = "",
                fechaCreacion = System.currentTimeMillis(),
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
            viewModelScope.launch {
                stopTrackingUseCase.execute()
                _uiState.value = TrackingUiState()
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
