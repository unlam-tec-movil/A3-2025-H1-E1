package ar.edu.unlam.scaffoldingandroid3.ui.navigation

/**
 * TODO: Sealed class - Destinos de navegación
 * Objects: PhotoPreview, RouteDetail
 * Incluir argumentos necesarios (routeId, photoId, etc.)
 */

sealed class Screen(val route: String) {
    object Tracking : Screen("tracking")

    object MyRoutes : Screen("myRoutes")

    object Map : Screen("map")

    object History : Screen("history")

    object RouteDetail : Screen("route/{routeId}") {
        fun createRoute(routeId: String) = "route/$routeId"
    }
}
