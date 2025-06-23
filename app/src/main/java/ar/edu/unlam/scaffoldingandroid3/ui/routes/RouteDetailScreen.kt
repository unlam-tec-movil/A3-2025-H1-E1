package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint
import ar.edu.unlam.scaffoldingandroid3.domain.model.Photo
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.ui.shared.ErrorDialog
import ar.edu.unlam.scaffoldingandroid3.ui.shared.LoadingSpinner
import ar.edu.unlam.scaffoldingandroid3.ui.theme.ScaffoldingAndroid3Theme

/**
 * Composable - Pantalla de detalle completo de ruta
 * Muestra información detallada de una ruta específica y permite iniciar su seguimiento.
 *
 * @param routeId Identificador de la ruta a mostrar
 * @param onStartClick Callback para iniciar el seguimiento de la ruta
 * @param viewModel ViewModel que maneja la lógica de la pantalla
 */

@Composable
fun RouteDetailScreen(
    routeId: String,
    onStartClick: () -> Unit,
    viewModel: RouteDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
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
                        onDismiss = viewModel::retryLoading,
                    )
                }
                uiState.route != null -> {
                    RouteDetailCard(
                        route = uiState.route!!,
                        photos = uiState.photos,
                        onStartClick = onStartClick,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RouteDetailScreenPreview() {
    val sampleRoute = Route(
        id = "1",
        name = "Ruta por el Parque",
        points = listOf(
            Route.Point(
                latitude = -34.6037,
                longitude = -58.3816,
                timestamp = System.currentTimeMillis(),
            ),
            Route.Point(
                latitude = -34.6038,
                longitude = -58.3817,
                timestamp = System.currentTimeMillis() + 1000,
            ),
        ),
        distance = 5200.0, // 5.2 km
        duration = 1800000, // 30 minutos
    )
    val samplePhotos = listOf(
        Photo(
            id = 1,
            uri = "https://picsum.photos/200/300",
            timestamp = System.currentTimeMillis(),
            location = LocationPoint(
                accuracy = 5f,
                speed = 0f,
                altitude = 100.0,
                latitude = -34.6037,
                longitude = -58.3816,
                timestamp = System.currentTimeMillis(),
            ),
            description = "Inicio de la ruta",
        ),
    )

    ScaffoldingAndroid3Theme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            RouteDetailCard(
                route = sampleRoute,
                photos = samplePhotos,
                onStartClick = {},
            )
        }
    }
}
