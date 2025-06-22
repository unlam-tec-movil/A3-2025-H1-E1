package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.ui.shared.ErrorDialog
import ar.edu.unlam.scaffoldingandroid3.ui.shared.LoadingSpinner

/**
 * TODO
 * Agregar @parcelable a Route para pasarle el objeto entero
 * a RouteDetailScreen
 */
@Composable
fun MyRoutesScreen(
    viewModel: MyRoutesViewModel = hiltViewModel(),
    onRouteClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if(uiState.isLoading) {
        LoadingSpinner()
    }
    uiState.error?.let{
        ErrorDialog(
            errorMessage = it,
            onDismiss = { viewModel.clearError() }
        )
    }
    uiState.emptyMessage?.let{
        ErrorDialog(
            errorMessage = it,
            onDismiss = { viewModel.clearError() }
        )
    }
    RouteList(
        list = uiState.savedRoutes,
        onPlayClick = onRouteClick
    )

//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        contentAlignment = Alignment.Center,
//    ) {
//        if(uiState.isLoading) {
//            LoadingSpinner()
//        }
//        uiState.error?.let{
//            ErrorDialog(
//                errorMessage = it,
//                onDismiss = { viewModel.clearError() }
//            )
//        }
//        uiState.emptyMessage?.let{
//            ErrorDialog(
//                errorMessage = it,
//                onDismiss = { viewModel.clearError() }
//            )
//        }
//        RouteList(
//            list = uiState.savedRoutes,
//            onPlayClick = onRouteClick
//        )
//    }
}
