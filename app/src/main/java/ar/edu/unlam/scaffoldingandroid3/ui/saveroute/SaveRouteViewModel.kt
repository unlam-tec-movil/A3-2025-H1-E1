package ar.edu.unlam.scaffoldingandroid3.ui.saveroute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingResult
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
    private val saveTrackingResultUseCase: SaveTrackingResultUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SaveRouteUiState())
    val uiState: StateFlow<SaveRouteUiState> = _uiState.asStateFlow()
    
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
    fun saveTrackingResult(trackingResult: TrackingResult, onSuccess: () -> Unit, onError: (String) -> Unit) {
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