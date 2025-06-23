package ar.edu.unlam.scaffoldingandroid3.ui.routes

import ar.edu.unlam.scaffoldingandroid3.domain.model.Route

/**
 * Data class - Estado UI de lista de rutas favoritas
 */

data class MyRoutesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val savedRoutes: List<Route> = emptyList(),
    val emptyMessage: String? = null,
)
