package ar.edu.unlam.scaffoldingandroid3.ui.navigation

/**
 * TODO: Sealed class - Destinos de navegación
 * Objects: PhotoPreview
 * Incluir argumentos necesarios (routeId, photoId, etc.)
 */

sealed class Screen(val route: String) {
    object Tracking : Screen("tracking")

    object MyRoutes : Screen("myRoutes")

    object Map : Screen("map")

    object History : Screen("history")

    object SaveRoute : Screen("save_route")

    object RouteDetail : Screen("route/{routeId}") {
        fun createRoute(routeId: String) = "route/$routeId"
    }
}
