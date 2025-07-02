package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.BuildConfig
import ar.edu.unlam.scaffoldingandroid3.domain.logic.RouteDisplayCalculator
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics
import ar.edu.unlam.scaffoldingandroid3.ui.routes.dto.TrackingMetricsDto
import ar.edu.unlam.scaffoldingandroid3.ui.routes.dto.toDomain
import ar.edu.unlam.scaffoldingandroid3.ui.shared.generateStaticMapUrl
import ar.edu.unlam.scaffoldingandroid3.ui.theme.ScaffoldingAndroid3Theme
import coil.compose.AsyncImage

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
    viewModel: RouteDetailViewModel = hiltViewModel(),
    isFromHistory: Boolean = false,
    historyMetricsDto: TrackingMetricsDto? = null,
    routeDisplayCalculator: RouteDisplayCalculator = RouteDisplayCalculator(),
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val mapHeight = (screenWidth * 0.6f)
    
    val uiState by viewModel.uiState.collectAsState()
    var showMessage by remember { mutableStateOf<String?>(null) }

    val staticMapUrl =
        remember(route) {
            generateStaticMapUrl(
                route = route,
                apiKey = BuildConfig.MAPS_API_KEY,
                width = (screenWidth.value * 2).toInt(),
                height = (mapHeight.value * 2).toInt(),
                scale = 2,
            )
        }
    
    // Verificar si la ruta ya está guardada al cargar la pantalla (solo si no viene del historial)
    LaunchedEffect(route) {
        if (!isFromHistory) {
            viewModel.checkIfRouteSaved(route)
        }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = route.name, 
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Botón de guardar/descarga (solo si no viene del historial)
                if (!isFromHistory) {
                    when {
                        uiState.isSaving -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        uiState.isSaved -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Ruta guardada",
                                tint = Color.Green,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        else -> {
                            IconButton(
                                onClick = {
                                    viewModel.saveRoute(
                                        route = route,
                                        onSuccess = {
                                            showMessage = "Ruta guardada correctamente"
                                        },
                                        onError = { error ->
                                            showMessage = error
                                        }
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Guardar ruta",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isFromHistory) {
                    "Distancia recorrida: ${String.format("%.0f", route.distance * 1000)} m"
                } else {
                    "Distancia: ${String.format("%.0f", routeDisplayCalculator.getDistanceInMeters(route))} m"
                }, 
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            if (isFromHistory) {
                Text(
                    text = "Tiempo empleado: ${routeDisplayCalculator.formatDuration(route.duration)}", 
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                val estimatedDuration = routeDisplayCalculator.calculateEstimatedDuration(route)
                Text(
                    text = "Duración estimada: ${routeDisplayCalculator.formatDuration(estimatedDuration)}", 
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            // Mostrar métricas detalladas si están disponibles (solo desde historial)
            historyMetricsDto?.let { metricsDto ->
                Spacer(modifier = Modifier.height(16.dp))
                DetailedMetricsSection(metrics = metricsDto.toDomain())
            }
            
            // Mostrar mensaje si existe
            showMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (message.contains("Error")) MaterialTheme.colorScheme.error else Color.Green
                )
                
                // Limpiar mensaje después de 3 segundos
                LaunchedEffect(message) {
                    kotlinx.coroutines.delay(3000)
                    showMessage = null
                }
            }
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
            Text(
                text = if (isFromHistory) "Ver en mapa" else "Iniciar recorrido", 
                style = MaterialTheme.typography.titleMedium
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
        RouteDetailScreen(
            route = sampleRoute,
            onStartClick = {},
        )
    }
}

@Composable
private fun DetailedMetricsSection(metrics: TrackingMetrics) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Métricas del recorrido",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Primera fila: Pasos y Velocidades
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricItem(
                label = "Pasos",
                value = "${metrics.totalSteps}",
                modifier = Modifier.weight(1f)
            )
            MetricItem(
                label = "Vel. Promedio",
                value = "${String.format("%.1f", metrics.averageSpeed)} km/h",
                modifier = Modifier.weight(1f)
            )
        }
        
        // Segunda fila: Velocidad máxima y Altitudes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricItem(
                label = "Vel. Máxima",
                value = "${String.format("%.1f", metrics.maxSpeed)} km/h",
                modifier = Modifier.weight(1f)
            )
            MetricItem(
                label = "Alt. Máxima",
                value = "${String.format("%.0f", metrics.maxElevation)} m",
                modifier = Modifier.weight(1f)
            )
        }
        
        // Tercera fila: Altitud mínima (si es diferente de la máxima)
        if (metrics.minElevation != metrics.maxElevation) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Alt. Mínima",
                    value = "${String.format("%.0f", metrics.minElevation)} m",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
