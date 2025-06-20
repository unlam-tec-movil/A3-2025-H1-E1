package ar.edu.unlam.scaffoldingandroid3.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.GetCurrentLocationUseCase
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.GetNearbyRoutesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.location.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getNearbyRoutesUseCase: GetNearbyRoutesUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    fun onPermissionResult(isGranted: Boolean) {
        // Previene la re-inicialización si ya tenemos una ubicación.
        if (_uiState.value.currentLocation != null) return

        viewModelScope.launch {
            val location = if (isGranted) getCurrentLocationUseCase() else null

            if (location != null) {
                _uiState.update {
                    it.copy(
                        currentLocation = location,
                        isLocationEnabled = true
                    )
                }
                // Mueve la cámara a la posición obtenida
                _uiState.value.cameraPositionState.move(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            11f
                        )
                    )
                )
                fetchNearbyRoutes(location)
            } else {
                // Si el permiso fue denegado o la ubicación no se pudo obtener, usa la ubicación por defecto.
                val defaultLocation = LocationPoint(
                    latitude = -34.6037,
                    longitude = -58.3816,
                    accuracy = 0f,
                    speed = null,
                    altitude = null,
                    timestamp = System.currentTimeMillis()
                )
                _uiState.update {
                    it.copy(
                        currentLocation = defaultLocation,
                        isLocationEnabled = false
                    )
                }
                // Mueve la cámara a la posición por defecto
                _uiState.value.cameraPositionState.move(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(
                            LatLng(defaultLocation.latitude, defaultLocation.longitude),
                            11f
                        )
                    )
                )
                fetchNearbyRoutes(defaultLocation)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onMapIdle(cameraPosition: CameraPosition) {
        val newCenter = Location("").apply {
            latitude = cameraPosition.target.latitude
            longitude = cameraPosition.target.longitude
        }

        val lastLocation = _uiState.value.lastSearchedLocation?.let {
            Location("").apply {
                latitude = it.latitude
                longitude = it.longitude
            }
        } ?: return

        if (newCenter.distanceTo(lastLocation) > 1500) { // Umbral de 1500 metros
            _uiState.update { it.copy(showSearchInAreaButton = true) }
        }
    }

    fun searchInMapArea(newCenter: LatLng) {
        val location = LocationPoint(
            latitude = newCenter.latitude,
            longitude = newCenter.longitude,
            accuracy = 0f,
            speed = null,
            altitude = null,
            timestamp = System.currentTimeMillis()
        )
        _uiState.update { it.copy(showSearchInAreaButton = false) }
        fetchClosestRoutesInArea(location)
    }

    fun onMessageShown() {
        _uiState.update { it.copy(showNoResultsMessage = false) }
    }

    private fun fetchClosestRoutesInArea(location: LocationPoint) {
        _uiState.update { it.copy(isLoading = true, lastSearchedLocation = location) }
        viewModelScope.launch {
            val result = getNearbyRoutesUseCase(
                lat = location.latitude,
                lon = location.longitude,
                radius = 50000, // 50km de radio
                limit = 10
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        nearbyRoutes = result.getOrThrow(),
                        showNoResultsMessage = result.getOrThrow().isEmpty()
                    )
                }
            } else {
                val error = result.exceptionOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error?.message ?: "Error al buscar rutas."
                    )
                }
            }
        }
    }

    private fun fetchNearbyRoutes(
        location: LocationPoint,
        radii: List<Int> = listOf(15000, 20000, 25000),
        limit: Int? = null
    ) {
        _uiState.update { it.copy(isLoading = true, lastSearchedLocation = location) }
        viewModelScope.launch {
            for (radius in radii) {
                val result = getNearbyRoutesUseCase(
                    location.latitude,
                    location.longitude,
                    radius,
                    limit
                )

                if (result.isSuccess) {
                    val routes = result.getOrThrow()
                    if (routes.size >= 3) {
                        _uiState.update { it.copy(isLoading = false, nearbyRoutes = routes) }
                        return@launch
                    }
                } else {
                    val error = result.exceptionOrNull()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error?.message ?: "Error al buscar rutas cercanas."
                        )
                    }
                    return@launch
                }
            }
            // Si después de todos los intentos no hay 3 o más rutas, mostramos lo que haya
            // y si no hay nada, activamos el mensaje.
            val finalRoutes = getNearbyRoutesUseCase(
                location.latitude,
                location.longitude,
                radii.last(),
                limit
            ).getOrNull() ?: emptyList()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    nearbyRoutes = finalRoutes,
                    showNoResultsMessage = finalRoutes.isEmpty()
                )
            }
        }
    }
}
