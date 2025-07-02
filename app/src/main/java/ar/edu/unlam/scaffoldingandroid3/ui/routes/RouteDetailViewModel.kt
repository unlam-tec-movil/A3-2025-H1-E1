package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.SaveApiRouteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para RouteDetailScreen
 * Maneja la funcionalidad de guardar rutas de la API en el almacenamiento local
 */
@HiltViewModel
class RouteDetailViewModel
    @Inject
    constructor(
        private val saveApiRouteUseCase: SaveApiRouteUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(RouteDetailUiState())
        val uiState: StateFlow<RouteDetailUiState> = _uiState.asStateFlow()

        /**
         * Guarda una ruta de la API en el almacenamiento local
         */
        fun saveRoute(
            route: Route,
            onSuccess: () -> Unit,
            onError: (String) -> Unit,
        ) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isSaving = true)

                saveApiRouteUseCase.execute(route)
                    .onSuccess {
                        _uiState.value =
                            _uiState.value.copy(
                                isSaving = false,
                                isSaved = true,
                            )
                        onSuccess()
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(isSaving = false)
                        onError("Error al guardar la ruta: ${exception.message}")
                    }
            }
        }

        /**
         * Verifica si una ruta ya está guardada localmente
         */
        fun checkIfRouteSaved(route: Route) {
            viewModelScope.launch {
                try {
                    val isAlreadySaved = saveApiRouteUseCase.isAlreadySaved(route)
                    _uiState.value = _uiState.value.copy(isSaved = isAlreadySaved)
                } catch (e: Exception) {
                    // Error no crítico, continuar normalmente
                }
            }
        }

        fun resetSavedStatus() {
            _uiState.value = _uiState.value.copy(isSaved = false)
        }
    }
