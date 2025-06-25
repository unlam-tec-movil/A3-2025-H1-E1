package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de detalle de ruta.
 * Maneja la carga de datos de la ruta y sus fotos asociadas.
 */

@HiltViewModel
class RouteDetailViewModel
@Inject constructor(
//    private val routeRepository: RouteRepository,
////    savedStateHandle: SavedStateHandle,
) : ViewModel() {
//    private val routeId: String = checkNotNull(savedStateHandle["routeId"])
//    private val _uiState = MutableStateFlow(RouteDetailUiState())
//    val uiState: StateFlow<RouteDetailUiState> = _uiState
//
//    init {
//        loadRouteDetails()
//    }
//
//    private fun loadRouteDetails() {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true, error = null) }
//            try {
//                val route = routeRepository.getRoute(routeId)
//                if (route != null) {
//                    // TODO: Implement getPhotosByRoute in PhotoRepository
//                    // val photos = photoRepository.getPhotosByRoute(routeId)
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            route = route,
//                            error = null,
//                        )
//                    }
//                } else {
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            error = "Ruta no encontrada",
//                        )
//                    }
//                }
//            } catch (e: Exception) {
//                _uiState.update {
//                    it.copy(
//                        isLoading = false,
//                        error = e.message ?: "Error al cargar la ruta",
//                    )
//                }
//            }
//        }
//    }
//
//    fun clearError() {
//        _uiState.update { it.copy(error = null) }
//    }
}
