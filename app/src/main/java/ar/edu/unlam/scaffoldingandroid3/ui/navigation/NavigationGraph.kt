package ar.edu.unlam.scaffoldingandroid3.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ar.edu.unlam.scaffoldingandroid3.ui.explore.MapScreen
import ar.edu.unlam.scaffoldingandroid3.ui.history.HistoryScreen
import ar.edu.unlam.scaffoldingandroid3.ui.routes.MyRoutesScreen
import ar.edu.unlam.scaffoldingandroid3.ui.routes.RouteDetailScreen
import ar.edu.unlam.scaffoldingandroid3.ui.tracking.TrackingScreen

/**
 * Composable - NavHost principal de la app
 * TODO: Configurar rutas: photo, route_detail
 * -Usar BottomNavigation + NavigationActions
 * -Separar UI vs navegación: las pantallas (Screens) no conocen al NavController, sólo reciben
 * el callback de navegación como lambdas.
 */

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Map.route,
        modifier = modifier,
    ) {
        composable(Screen.Tracking.route) {
            TrackingScreen(
                onNavigationBack = {
                    navController.popBackStack() // vuelve a la última en el stack (Map)
                },
            )
        }
        composable(Screen.MyRoutes.route) {
            MyRoutesScreen(
                onRouteClick = { routeId ->
                    navController.navigate(Screen.RouteDetail.createRoute(routeId))
                }
            )
        }
        composable(Screen.Map.route) {
            MapScreen(
                onNewRouteClick = {
                    navController.navigate(Screen.Tracking.route)
                },
                onLoadRoutesClick = {},
                onRouteClick = { routeId ->
                    navController.navigate(Screen.RouteDetail.createRoute(routeId))
                },
            )
        }
        composable(Screen.History.route) {
            HistoryScreen()
        }
        composable(Screen.RouteDetail.route) {
            RouteDetailScreen()
        }
    }
}
