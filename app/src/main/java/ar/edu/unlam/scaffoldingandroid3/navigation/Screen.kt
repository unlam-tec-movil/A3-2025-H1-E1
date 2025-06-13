package ar.edu.unlam.scaffoldingandroid3.navigation

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
}
