package ar.edu.unlam.scaffoldingandroid3.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.ui.explore.MapScreen
import ar.edu.unlam.scaffoldingandroid3.ui.history.HistoryScreen
import ar.edu.unlam.scaffoldingandroid3.ui.routes.MyRoutesScreen
import ar.edu.unlam.scaffoldingandroid3.ui.routes.RouteDetailScreen
import ar.edu.unlam.scaffoldingandroid3.ui.saveroute.SaveRouteScreen
import ar.edu.unlam.scaffoldingandroid3.ui.tracking.TrackingScreen

/**
 * Composable - NavHost principal de la app
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
                navController = navController,
            )
        }
        composable(Screen.Map.route) {
            MapScreen(
                navController = navController,
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                navController = navController,
            )
        }
        composable(Screen.RouteDetail.route) { backStackEntry ->
            // 1) Leemos desde el savedStateHandle de ESTA entrada
            val route: Route? = backStackEntry.savedStateHandle["route"]

            if (route != null) {
                RouteDetailScreen(
                    route = route,
                    onStartClick = {
                        navController.navigate(Screen.Tracking.route)
                    },
                )
            } else {
                // 2) Fallback: si no llega nada, volvemos atrás
                LaunchedEffect(Unit) {
                    navController.navigateUp()
                }
            }
        }
        composable(Screen.SaveRoute.route) {
            SaveRouteScreen(
                onNavigateBack = {
                    // Limpiar datos temporales y volver al mapa
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Map.route) { inclusive = false }
                    }
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
