package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.ui.theme.ScaffoldingAndroid3Theme

/**
 * Composable - Pantalla de detalle completo de ruta
 * Muestra información detallada de una ruta específica y permite iniciar su seguimiento.
 *
 * @param route ruta a mostrar
 * @param onStartClick Callback para iniciar el seguimiento de la ruta
 */

@Composable
fun RouteDetailScreen(
    route: Route,
    onStartClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            RouteDetailCard(
                route = route,
                onStartClick = onStartClick,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RouteDetailScreenPreview() {
    val sampleRoute =
        Route(
            id = "1",
            name = "Ruta por el Parque",
            points =
                listOf(
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
            // 5.2 km
            distance = 5200.0,
            // 30 minutos
            duration = 1800000,
        )

    ScaffoldingAndroid3Theme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            RouteDetailCard(
                route = sampleRoute,
                onStartClick = {},
            )
        }
    }
}
