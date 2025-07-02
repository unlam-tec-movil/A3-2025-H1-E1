package ar.edu.unlam.scaffoldingandroid3.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.ui.explore.MapScreen
import ar.edu.unlam.scaffoldingandroid3.ui.history.HistoryScreen
import ar.edu.unlam.scaffoldingandroid3.ui.routes.MyRoutesScreen
import ar.edu.unlam.scaffoldingandroid3.ui.routes.RouteDetailScreen
import ar.edu.unlam.scaffoldingandroid3.ui.routes.dto.TrackingMetricsDto
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
        composable(
            route = "${Screen.Tracking.route}?${Screen.Tracking.ARG_FOLLOW_ID}={${Screen.Tracking.ARG_FOLLOW_ID}}",
            arguments =
                listOf(
                    navArgument(Screen.Tracking.ARG_FOLLOW_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
        ) { backStackEntry ->
            val followId = backStackEntry.arguments?.getString(Screen.Tracking.ARG_FOLLOW_ID)

            TrackingScreen(
                onNavigationBack = { navController.popBackStack() },
                onTrackingCompleted = {
                    if (followId == null) {
                        navController.navigate(Screen.SaveRoute.route)
                    } else {
                        navController.navigate(Screen.Map.route) {
                            popUpTo(Screen.Map.route) { inclusive = false }
                        }
                    }
                },
            )
        }

        // Variante para rutas pasadas como objeto (rutas de Overpass)
        composable(Screen.Tracking.route) { backStackEntry ->
            val savedRoute: Route? = backStackEntry.savedStateHandle["followRoute"]

            TrackingScreen(
                onNavigationBack = { navController.popBackStack() },
                followRoute = savedRoute,
                onTrackingCompleted = {
                    if (savedRoute == null) {
                        navController.navigate(Screen.SaveRoute.route)
                    } else {
                        navController.navigate(Screen.Map.route) {
                            popUpTo(Screen.Map.route) { inclusive = false }
                        }
                    }
                },
            )
        }

        composable(Screen.MyRoutes.route) {
            MyRoutesScreen(navController = navController)
        }

        composable(Screen.Map.route) { backStackEntry ->
            val selectedRoute: Route? = backStackEntry.savedStateHandle.remove("selectedRoute")

            MapScreen(navController = navController, selectedRoute = selectedRoute)
        }

        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }

        composable(Screen.RouteDetail.route) { backStackEntry ->
            val route: Route? = backStackEntry.savedStateHandle["route"]
            val isFromHistory: Boolean = backStackEntry.savedStateHandle["isFromHistory"] ?: false
            val historyMetrics: TrackingMetricsDto? = backStackEntry.savedStateHandle["historyMetrics"]

            if (route != null) {
                RouteDetailScreen(
                    route = route,
                    isFromHistory = isFromHistory,
                    historyMetricsDto = historyMetrics,
                    onStartClick = {
                        if (isFromHistory) {
                            // Si viene del historial, ir al mapa con la ruta seleccionada
                            navController.navigate(Screen.Map.route)
                            navController.getBackStackEntry(Screen.Map.route).savedStateHandle["selectedRoute"] = route
                        } else {
                            // Detectar si es ruta local (UUID) o ruta de Overpass (número)
                            val isLocalRoute = route.id.contains("-") // UUIDs contienen guiones

                            if (isLocalRoute) {
                                // Ruta guardada localmente - usar ID
                                navController.navigate("${Screen.Tracking.route}?${Screen.Tracking.ARG_FOLLOW_ID}=${route.id}")
                            } else {
                                // Ruta de Overpass - pasar objeto directamente
                                navController.navigate(Screen.Tracking.route)
                                // Setear el objeto en el nuevo entry después de navegar
                                navController.getBackStackEntry(Screen.Tracking.route).savedStateHandle["followRoute"] = route
                            }
                        }
                    },
                )
            } else {
                LaunchedEffect(Unit) { navController.navigateUp() }
            }
        }

        composable(Screen.SaveRoute.route) {
            SaveRouteScreen(
                onNavigateBack = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Map.route) { inclusive = false }
                    }
                },
                onSaveRoute = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Map.route) { inclusive = false }
                    }
                },
                onDiscardRoute = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Map.route) { inclusive = false }
                    }
                },
            )
        }
    }
}
