package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.unlam.scaffoldingandroid3.BuildConfig
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.ui.shared.generateStaticMapUrl
import ar.edu.unlam.scaffoldingandroid3.ui.theme.ScaffoldingAndroid3Theme
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * Composable - Card expandido con detalles completos de ruta
 * UI: Imagen, metadata, descripción, carrusel de fotos, botón "Iniciar"
 *
 * @param route Ruta a mostrar
 * @param onStartClick Acción al hacer clic en el botón "Iniciar"
 */
@Composable
fun RouteDetailCard(
    route: Route,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Parámetros para la imagen de mapa estático
    val apiKey = remember { BuildConfig.MAPS_API_KEY }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val mapHeight =
        remember {
            (screenWidth * 0.6f)
        }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            // Mapa de la ruta
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(mapHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray),
            ) {
                val staticMapUrl =
                    remember(route, apiKey) {
                        generateStaticMapUrl(
                            route = route,
                            apiKey = apiKey,
                            width = screenWidth.value.toInt().coerceAtLeast(200),
                            height = mapHeight.value.toInt().coerceAtLeast(150),
                            scale = 2,
                        )
                    }

                SubcomposeAsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(staticMapUrl)
                            .crossfade(true)
                            .build(),
                    contentDescription = "Mapa de la ruta",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Metadata de la ruta
            Text(
                text = route.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Descripción de la ruta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    val routeDisplayCalculator = ar.edu.unlam.scaffoldingandroid3.domain.logic.RouteDisplayCalculator()
                    Text(
                        text = "Distancia: ${String.format("%.0f", routeDisplayCalculator.getDistanceInMeters(route))} m",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Column {
                    val routeDisplayCalculator = ar.edu.unlam.scaffoldingandroid3.domain.logic.RouteDisplayCalculator()
                    Text(
                        text = "Duración: ${routeDisplayCalculator.formatDuration(routeDisplayCalculator.calculateEstimatedDuration(route))}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
//             TODO Agregar foto
//            carrusel de fotos (deprecado)
//            if (photos.isNotEmpty()) {
//                val pagerState =
//                    rememberPagerState(
//                        pageCount = { photos.size },
//                    )
//                Text(
//                    text = "Fotos de la ruta",
//                    style = MaterialTheme.typography.titleMedium,
//                    maxLines = 1,
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                HorizontalPager(
//                    state = pagerState,
//                    modifier =
//                        Modifier
//                            .fillMaxWidth()
//                            .height(120.dp),
//                ) { page ->
//                    AsyncImage(
//                        model = photos[page].uri,
//                        contentDescription = null,
//                        modifier =
//                            Modifier
//                                .fillMaxWidth()
//                                .padding(4.dp)
//                                .clip(RoundedCornerShape(8.dp)),
//                        contentScale = ContentScale.Crop,
//                    )
//                }
//            }
            Spacer(modifier = Modifier.height(8.dp))
            // Botón "Iniciar"
            Button(
                onClick = onStartClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Iniciar ruta",
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Iniciar Ruta",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RouteDetailCardPreview() {
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
//    val samplePhotos =
//        listOf(
//            Photo(
//                id = 1,
//                uri = "https://picsum.photos/200/300",
//                timestamp = System.currentTimeMillis(),
//                location =
//                    LocationPoint(
//                        accuracy = 5f,
//                        speed = 0f,
//                        altitude = 100.0,
//                        latitude = -34.6037,
//                        longitude = -58.3816,
//                        timestamp = System.currentTimeMillis(),
//                    ),
//                description = "Inicio de la ruta",
//            ),
//            Photo(
//                id = 2,
//                uri = "https://picsum.photos/200/301",
//                timestamp = System.currentTimeMillis(),
//                location =
//                    LocationPoint(
//                        accuracy = 5f,
//                        speed = 0f,
//                        altitude = 100.0,
//                        latitude = -34.6038,
//                        longitude = -58.3817,
//                        timestamp = System.currentTimeMillis(),
//                    ),
//                description = "Punto intermedio",
//            ),
//        )
    ScaffoldingAndroid3Theme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
        ) {
            RouteDetailCard(
                route = sampleRoute,
//                photos = samplePhotos,
                onStartClick = {},
            )
        }
    }
}
