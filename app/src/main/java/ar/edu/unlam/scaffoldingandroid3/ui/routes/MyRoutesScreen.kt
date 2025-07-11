package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import ar.edu.unlam.scaffoldingandroid3.ui.navigation.Screen
import ar.edu.unlam.scaffoldingandroid3.ui.shared.ErrorDialog
import ar.edu.unlam.scaffoldingandroid3.ui.shared.LoadingSpinner

/**
 * Pantalla MyRoutesScreen
 * Muestra la lista de rutas guardadas del usuario, gestionando estados de carga, error y vacío.
 * Permite navegar al detalle de una ruta seleccionada.
 */
@Composable
fun MyRoutesScreen(
    viewModel: MyRoutesViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    val uiState by viewModel.uiState.collectAsState()
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isLoading -> LoadingSpinner()

            uiState.error != null ->
                ErrorDialog(
                    errorMessage = uiState.error!!,
                    onDismiss = { viewModel.clearError() },
                )

            uiState.emptyMessage != null -> Text(text = uiState.emptyMessage!!)

            else -> {
                RouteList(
                    routeList = uiState.savedRoutes,
//                    routeList = listOf(
//                        Route("1", "Ruta 1", emptyList(), 10.00, 8400000),
//                        Route("2", "Ruta 2", emptyList(), 10.00, 8400000),
//                        Route("3", "Ruta 3", emptyList(), 10.00, 8400000),
//                        Route("4", "Ruta 4", emptyList(), 10.00, 8400000),
//                    ),
                    onPlayClick = { selectedRoute ->
                        navController.navigate(Screen.RouteDetail.route)
                        navController.getBackStackEntry(Screen.RouteDetail.route).savedStateHandle["route"] =
                            selectedRoute
                    },
                    onDeleteItem = { routeId ->
                        viewModel.deleteRouteItem(routeId)
                    },
                )
            }
        }
    }
}
