package ar.edu.unlam.scaffoldingandroid3.ui.explore

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route

/**
 * Data class que representa el estado de la UI para la pantalla del mapa de exploración.
 *
 * @property isLoading indica si se está cargando información (ej: rutas cercanas).
 * @property isLocationEnabled indica si la ubicación del usuario está habilitada.
 * @property currentLocation la última ubicación conocida del usuario.
 * @property nearbyRoutes la lista de rutas de trekking cercanas a mostrar en el mapa.
 * @property error un mensaje de error a mostrar, si lo hubiera.
 * @property lastSearchedLocation la última ubicación buscada por el usuario.
 * @property showSearchInAreaButton indica si el botón de búsqueda en área está visible.
 * @property showNoResultsMessage indica si el mensaje de "no hay resultados" está visible.
 */
data class MapUiState(
    val isLoading: Boolean = true,
    val isLocationEnabled: Boolean = false,
    val currentLocation: LocationPoint? = null,
    val nearbyRoutes: List<Route> = emptyList(),
    val error: String? = null,
    val lastSearchedLocation: LocationPoint? = null,
    val showSearchInAreaButton: Boolean = false,
    val showNoResultsMessage: Boolean = false
)
