package ar.edu.unlam.scaffoldingandroid3.ui.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.domain.model.History
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.ui.navigation.Screen
import ar.edu.unlam.scaffoldingandroid3.ui.routes.dto.toDto
import ar.edu.unlam.scaffoldingandroid3.ui.shared.ErrorDialog
import ar.edu.unlam.scaffoldingandroid3.ui.shared.LoadingSpinner

/**
 * Pantalla de historial de actividades
 * Usa el viewModel que nos traiga la info de la base de datos
 * y la convierta a domain para poder pasarsela a la lista cuando
 * eso se implemente
 */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
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
            uiState.isLoading -> {
                LoadingSpinner()
            }
            uiState.error != null -> {
                ErrorDialog(
                    errorMessage = uiState.error!!,
                    onDismiss = { viewModel.clearError() },
                )
            }
            uiState.isEmpty -> {
                Text(
                    text = stringResource(id = R.string.history_empty_message),
                    modifier = Modifier.padding(16.dp),
                )
            }
            else -> {
                HistoryList(
                    historyList = uiState.historyList,
                    onDeleteItem = { historyId ->
                        viewModel.deleteHistoryItem(historyId)
                    },
                    onItemClick = { history ->
                        val route = history.toRoute()
                        navController.navigate(Screen.RouteDetail.route)
                        navController.getBackStackEntry(Screen.RouteDetail.route)
                            .savedStateHandle["route"] = route
                        navController.getBackStackEntry(Screen.RouteDetail.route)
                            .savedStateHandle["isFromHistory"] = true
                        navController.getBackStackEntry(Screen.RouteDetail.route)
                            .savedStateHandle["historyMetrics"] = history.metrics.toDto()
                    },
                )
            }
        }
    }
}

// Mapper provisional de History a Route porque RouteDetailScreen espera un Route
fun History.toRoute(): Route {
    return Route(
        id = this.id.toString(),
        name = this.routeName,
        points =
            this.routePoint.map {
                Route.Point(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    timestamp = it.timestamp,
                )
            },
        distance = this.metrics.currentDistance,
        duration = this.metrics.currentDuration,
        photoUri = this.photoUri,
    )
}
