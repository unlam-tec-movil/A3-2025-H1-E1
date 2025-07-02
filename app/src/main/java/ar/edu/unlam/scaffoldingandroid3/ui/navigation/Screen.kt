package ar.edu.unlam.scaffoldingandroid3.ui.navigation

/**
 * TODO: Sealed class - Destinos de navegación
 * Objects: PhotoPreview
 * Incluir argumentos necesarios (routeId, photoId, etc.)
 */

sealed class Screen(val route: String) {
    object Tracking : Screen("tracking") {
        const val ARG_FOLLOW_ID = "followId"

        fun routeWithFollowId(routeId: String) = "tracking?${ARG_FOLLOW_ID}=$routeId"
    }

    object MyRoutes : Screen("myRoutes")

    object Map : Screen("map")

    object History : Screen("history")

    object SaveRoute : Screen("save_route")

    object RouteDetail : Screen("route_detail")
}
