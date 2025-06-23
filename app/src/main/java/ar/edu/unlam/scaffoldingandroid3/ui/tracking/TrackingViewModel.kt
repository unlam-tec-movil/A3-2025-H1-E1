package ar.edu.unlam.scaffoldingandroid3.ui.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingStatus
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
 * ViewModel - Gestiona tracking con integración completa de sensores
 * 3 UI States: PREPARATION → RECORDING → EXPANDED_STATS
 * Integra: GPS, Acelerómetro, Barómetro, Magnetómetro
 * Respeta Clean Architecture usando Use Cases con lógica de negocio
 */
@HiltViewModel
class TrackingViewModel
    @Inject
    constructor(
        private val startTrackingUseCase: StartTrackingUseCase,
        private val stopTrackingUseCase: StopTrackingUseCase,
        private val updateTrackingUseCase: UpdateTrackingUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(TrackingUiState())
        val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

        private val _metrics = MutableStateFlow(TrackingMetrics())
        val metrics: StateFlow<TrackingMetrics> = _metrics.asStateFlow()

        private val _trackingStatus = MutableStateFlow(TrackingStatus.COMPLETED)
        val trackingStatus: StateFlow<TrackingStatus> = _trackingStatus.asStateFlow()

        // Estadísticas detalladas de todos los sensores
        private val _detailedStats = MutableStateFlow<Map<String, Any>>(emptyMap())
        val detailedStats: StateFlow<Map<String, Any>> = _detailedStats.asStateFlow()

        init {
            observeTrackingData()
        }

        private fun observeTrackingData() {
            viewModelScope.launch {
                // Observar estado de tracking
                updateTrackingUseCase.getTrackingStatus()
                    .catch { handleError("Error al observar estado: ${it.message}") }
                    .collect { status ->
                        _trackingStatus.value = status
                        updateUiStateForTrackingStatus(status)
                    }
            }

            viewModelScope.launch {
                // Observar métricas en tiempo real
                updateTrackingUseCase.getCurrentMetrics()
                    .catch { /* Métricas no críticas para UI */ }
                    .collect { metrics ->
                        _metrics.value = metrics
                        updateDetailedStats()
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
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                error = error.message ?: "Error al iniciar tracking",
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

        fun stopTracking(onCompleted: (TrackingSession) -> Unit) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)

                stopTrackingUseCase.execute()
                    .onSuccess { session ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        onCompleted(session)
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

        private fun handleError(message: String) {
            _uiState.value =
                _uiState.value.copy(
                    error = message,
                    isLoading = false,
                )
        }
    }
