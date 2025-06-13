package ar.edu.unlam.scaffoldingandroid3.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ar.edu.unlam.scaffoldingandroid3.ui.explore.MapScreen
import ar.edu.unlam.scaffoldingandroid3.ui.history.HistoryScreen
import ar.edu.unlam.scaffoldingandroid3.ui.routes.MyRoutesScreen

/**
 * Composable - NavHost principal de la app
 * TODO: Configurar rutas: tracking, photo, route_detail
 * -Usar BottomNavigation + NavigationActions
 * -Separar UI vs navegación: las pantallas (Screens) no conocen al NavController, sólo reciben
 * el callback de navegación como lambdas o viven dentro del NavHost.
 */

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
){
    NavHost(
        navController = navController,
        startDestination = Screen.Map.route,
        modifier = modifier
    ){
        composable(Screen.MyRoutes.route){
            MyRoutesScreen()
        }
        composable(Screen.Map.route){
            MapScreen(
                onNewRouteClick = {
                    // TODO: Implementar navegación a la pantalla de nueva ruta
                    // navegar a Screen.NewRoute.route
                },
                onLoadRoutesClick = {
                    // TODO: Implementar navegación a la pantalla de cargar rutas
                    // navegar a Screen.LoadRoutes.route
                },
            )
        }
        composable(Screen.History.route){
            HistoryScreen()
        }
    }
}
