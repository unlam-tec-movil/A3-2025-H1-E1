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
 * TODO: Composable - NavHost principal de la app
 * Configurar rutas: map, routes, history, tracking, photo, route_detail
 * Usar BottomNavigation + NavigationActions
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
        composable(Screen.Saved.route){
            MyRoutesScreen()
        }
        composable(Screen.Map.route){
            MapScreen(
                onNewRouteClick = {
                    // TODO: Implementar navegación a la pantalla de nueva ruta
                },
                onLoadRoutesClick = {
                    // TODO: Implementar navegación a la pantalla de cargar rutas
                },
            )
        }
        composable(Screen.History.route){
            HistoryScreen()
        }
    }
}
