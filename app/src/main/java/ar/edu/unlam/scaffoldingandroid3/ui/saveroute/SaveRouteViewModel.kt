package ar.edu.unlam.scaffoldingandroid3.ui.saveroute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingResult
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession
import ar.edu.unlam.scaffoldingandroid3.domain.repository.TrackingSessionRepository
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.SaveTrackingResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla SaveRoute
 * Compatible con Clean Architecture
 */
@HiltViewModel
class SaveRouteViewModel @Inject constructor(
    private val saveTrackingResultUseCase: SaveTrackingResultUseCase,
    private val trackingSessionRepository: TrackingSessionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SaveRouteUiState())
    val uiState: StateFlow<SaveRouteUiState> = _uiState.asStateFlow()
    
    private val _trackingResult = MutableStateFlow<TrackingResult?>(null)
    val trackingResult: StateFlow<TrackingResult?> = _trackingResult.asStateFlow()
    
    init {
        loadLastTrackingSession()
    }
    
    private fun loadLastTrackingSession() {
        viewModelScope.launch {
            try {
                // Obtener la última sesión completada desde el repository
                val session = trackingSessionRepository.getCurrentTrackingSession()
                session?.let {
                    val result = createTrackingResultFromSession(it)
                    _trackingResult.value = result
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    nameError = "Error al cargar datos del recorrido: ${e.message}"
                )
            }
        }
    }
    
    private fun createTrackingResultFromSession(session: TrackingSession): TrackingResult {
        val totalDuration = session.endTime - session.startTime
        val tiempoTotal = formatTime(totalDuration)
        val tiempoEnMovimiento = formatTime(session.metrics.currentDuration)
        
        return TrackingResult(
            tiempoTotal = tiempoTotal,
            tiempoEnMovimiento = tiempoEnMovimiento,
            distanciaTotal = session.metrics.currentDistance,
            pasosTotales = 0, // Simplificado por ahora
            velocidadMedia = session.metrics.averageSpeed,
            velocidadMaxima = session.metrics.maxSpeed,
            altitudMinima = session.metrics.currentElevation,
            altitudMaxima = session.metrics.currentElevation,
            rutaCompleta = session.routePoint,
            fotosCapturadas = emptyList(), // Se obtendrán del UI state
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
    
    fun updateRouteName(name: String) {
        _uiState.value = _uiState.value.copy(
            routeName = name,
            nameError = null // Limpiar error al escribir
        )
    }
    
    fun validateAndSave(): Boolean {
        val currentName = _uiState.value.routeName.trim()
        
        return if (currentName.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                nameError = "El nombre del recorrido es obligatorio"
            )
            false
        } else if (currentName.length > 50) {
            _uiState.value = _uiState.value.copy(
                nameError = "El nombre no puede tener más de 50 caracteres"
            )
            false
        } else {
            _uiState.value = _uiState.value.copy(
                nameError = null,
                isLoading = true
            )
            true
        }
    }
    
    fun showDiscardDialog() {
        _uiState.value = _uiState.value.copy(showDiscardDialog = true)
    }
    
    fun hideDiscardDialog() {
        _uiState.value = _uiState.value.copy(showDiscardDialog = false)
    }
    
    fun setLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = loading)
    }
    
    /**
     * Guarda el resultado de tracking en base de datos
     */
    fun saveTrackingResult(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val trackingResult = _trackingResult.value
        if (trackingResult == null) {
            onError("No hay datos de recorrido para guardar")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Actualizar nombre del recorrido con el ingresado por el usuario
            val updatedResult = trackingResult.copy(
                nombreRecorrido = _uiState.value.routeName.trim()
            )
            
            saveTrackingResultUseCase.execute(updatedResult)
                .onSuccess { sessionId ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError("Error al guardar recorrido: ${error.message}")
                }
        }
    }
}

/**
 * Estado de UI para SaveRoute
 */
data class SaveRouteUiState(
    val routeName: String = "",
    val nameError: String? = null,
    val isLoading: Boolean = false,
    val showDiscardDialog: Boolean = false,
)