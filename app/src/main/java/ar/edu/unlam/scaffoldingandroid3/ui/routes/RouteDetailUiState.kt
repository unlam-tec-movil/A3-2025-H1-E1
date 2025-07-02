package ar.edu.unlam.scaffoldingandroid3.ui.routes

import ar.edu.unlam.scaffoldingandroid3.domain.model.Route

/**
 * Estado de la UI para la pantalla de detalle de ruta.
 *
 * @property isLoading Indica si se están cargando los datos
 * @property route La ruta a mostrar (null si aún no se cargó)
 * @property error Mensaje de error si algo falló (null si no hay error)
 */

data class RouteDetailUiState(
    val isLoading: Boolean = false,
    val route: Route? = null,
    val error: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)
