package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.ui.theme.ScaffoldingAndroid3Theme
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalConfiguration
import ar.edu.unlam.scaffoldingandroid3.BuildConfig
import ar.edu.unlam.scaffoldingandroid3.ui.shared.generateStaticMapUrl
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

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
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val mapHeight = (screenWidth * 0.6f)

    val staticMapUrl = remember(route) {
        generateStaticMapUrl(
            route = route,
            apiKey = BuildConfig.MAPS_API_KEY,
            width = (screenWidth.value * 2).toInt(),
            height = (mapHeight.value * 2).toInt(),
            scale = 2,
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
    ) {
        // Imagen estática
        AsyncImage(
            model = staticMapUrl,
            contentDescription = "Mapa estático de la ruta",
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(mapHeight)
                    .padding(start = 8.dp, end = 8.dp, top = 16.dp)
                    .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
        )

        // Información de la ruta
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = route.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Distancia: ${String.format("%.1f", route.distance / 1000)} km", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Duración estimada: ${route.duration / (1000 * 60)} min", style = MaterialTheme.typography.bodyLarge)
            // Placeholder para otros datos cuando estén disponibles
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón iniciar
        Button(
            onClick = onStartClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(56.dp),
        ) {
            Text(text = "Iniciar recorrido", style = MaterialTheme.typography.titleMedium)
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
        RouteDetailScreen(
            route = sampleRoute,
            onStartClick = {},
        )
    }
}
