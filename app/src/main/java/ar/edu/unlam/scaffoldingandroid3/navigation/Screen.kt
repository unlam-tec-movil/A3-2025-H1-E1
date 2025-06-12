package ar.edu.unlam.scaffoldingandroid3.navigation

/**
 * TODO: Sealed class - Destinos de navegación
 * Objects: Map, MyRoutes, History, Tracking, PhotoPreview, RouteDetail
 * Incluir argumentos necesarios (routeId, photoId, etc.)
 */

sealed class Screen(val route: String) {
    object Saved : Screen("saved")
    object Map : Screen("map")
    object History : Screen("history")
}
