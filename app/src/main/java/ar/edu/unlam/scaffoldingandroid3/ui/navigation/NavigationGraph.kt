package ar.edu.unlam.scaffoldingandroid3.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ar.edu.unlam.scaffoldingandroid3.ui.explore.MapScreen
import ar.edu.unlam.scaffoldingandroid3.ui.history.HistoryScreen
import ar.edu.unlam.scaffoldingandroid3.ui.routes.MyRoutesScreen
import ar.edu.unlam.scaffoldingandroid3.ui.routes.RouteDetailScreen
import ar.edu.unlam.scaffoldingandroid3.ui.saveroute.SaveRouteScreen
import ar.edu.unlam.scaffoldingandroid3.ui.tracking.TrackingScreen

/**
 * Composable - NavHost principal de la app
 * TODO: Configurar rutas: photo, route_detail
 * -Usar BottomNavigation + NavigationActions
 * -Separar UI vs navegación: las pantallas (Screens) no conocen al NavController, sólo reciben
 * el callback de navegación como lambdas.
 */

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    // Eliminar TrackingResultHolder - usar mejor práctica: datos desde última sesión del repository
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
                onTrackingCompleted = { trackingResult ->
                    // Navegar directamente - SaveRouteScreen obtendrá datos del repository
                    navController.navigate(Screen.SaveRoute.route)
                },
            )
        }
        composable(Screen.MyRoutes.route) {
            MyRoutesScreen(
                onRouteClick = { routeId ->
                    navController.navigate(Screen.RouteDetail.createRoute(routeId))
                },
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
        composable(
            route = Screen.RouteDetail.route,
            arguments =
                listOf(
                    navArgument("routeId") {
                        type = NavType.StringType
                        nullable = false
                    },
                ),
        ) { backStackEntry ->
            // Extrae el valor de "routeId" desde los argumentos
            val routeId =
                backStackEntry.arguments?.getString("routeId")
                    ?: error("routeId es nulo")
            RouteDetailScreen(
                routeId = routeId,
                onStartClick = {
                    navController.navigate(Screen.Tracking.route)
                },
            )
        }
        composable(Screen.SaveRoute.route) {
            SaveRouteScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveRoute = { routeName ->
                    // Volver al mapa principal después de guardar
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Map.route) { inclusive = false }
                    }
                },
                onDiscardRoute = {
                    // Volver al mapa principal sin guardar
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Map.route) { inclusive = false }
                    }
                },
            )
        }
    }
}
