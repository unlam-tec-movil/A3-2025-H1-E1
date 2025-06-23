package ar.edu.unlam.scaffoldingandroid3.ui.tracking

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Pantalla de tracking con 3 estados y integración completa de sensores
 * PREPARATION → RECORDING → EXPANDED_STATS
 * Integra: GPS, Acelerómetro, Barómetro, Magnetómetro
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    onNavigationBack: () -> Unit,
    onTrackingCompleted: (TrackingSession) -> Unit = {},
    viewModel: TrackingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val metrics by viewModel.metrics.collectAsState()
    val detailedStats by viewModel.detailedStats.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // No auto-iniciar tracking, que el usuario decida

    // Manejo de errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text =
                            when (uiState.screenState) {
                                TrackingScreenState.PREPARATION -> "Nuevo Tracking"
                                TrackingScreenState.RECORDING -> "Grabando Ruta"
                                TrackingScreenState.EXPANDED_STATS -> "Estadísticas"
                            },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigationBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.screenState == TrackingScreenState.RECORDING) {
                FloatingActionButton(
                    onClick = { /* TODO: Implementar captura de foto */ },
                    containerColor = MaterialTheme.colorScheme.secondary,
                ) {
                    Icon(Icons.Default.PlayArrow, "Capturar foto")
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            // Pantalla única con mapa y botones dinámicos
            TrackingMapScreen(
                metrics = metrics,
                detailedStats = detailedStats,
                uiState = uiState,
                onStartClick = { viewModel.startTracking("Ruta ${System.currentTimeMillis()}") },
                onPauseClick = viewModel::pauseTracking,
                onResumeClick = viewModel::resumeTracking,
                onStopClick = {
                    viewModel.stopTracking { session ->
                        onTrackingCompleted(session)
                        onNavigationBack()
                    }
                },
                onExpandStats = viewModel::toggleStatsExpansion,
            )

            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun TrackingMapScreen(
    metrics: ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics,
    detailedStats: Map<String, Any>,
    uiState: TrackingUiState,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit,
    onExpandStats: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa de fondo
        ActiveTrackingMap(
            modifier = Modifier.fillMaxSize(),
        )

        // Botones superpuestos en la parte inferior
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            when (uiState.screenState) {
                TrackingScreenState.PREPARATION -> {
                    // Solo botón "Iniciar recorrido"
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Button(
                            onClick = onStartClick,
                            enabled = !uiState.isLoading,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(end = 8.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                            Text("Iniciar recorrido")
                        }
                    }
                }

                TrackingScreenState.RECORDING -> {
                    // Panel de métricas básicas con botones pause/stop
                    TrackingPanel(
                        metrics = metrics,
                        uiState = uiState,
                        onPauseClick = onPauseClick,
                        onResumeClick = onResumeClick,
                        onStopClick = onStopClick,
                        onExpandClick = onExpandStats,
                        isExpanded = false,
                    )
                }

                TrackingScreenState.EXPANDED_STATS -> {
                    // Panel expandido de estadísticas
                    TrackingPanel(
                        metrics = metrics,
                        uiState = uiState,
                        detailedStats = detailedStats,
                        onPauseClick = onPauseClick,
                        onResumeClick = onResumeClick,
                        onStopClick = onStopClick,
                        onExpandClick = onExpandStats,
                        isExpanded = true,
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveTrackingMap(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED,
        )
    }

    // Ubicación inicial (Buenos Aires)
    val defaultLocation = com.google.android.gms.maps.model.LatLng(-34.6037, -58.3816)
    val cameraPositionState =
        com.google.maps.android.compose.rememberCameraPositionState {
            position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(defaultLocation, 15f)
        }

    // Obtener la ubicación actual
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val fusedLocationClient =
                    com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)

                val location =
                    suspendCancellableCoroutine<android.location.Location?> { continuation ->
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location ->
                                continuation.resume(location)
                            }
                            .addOnFailureListener { e ->
                                continuation.resumeWithException(e)
                            }
                        continuation.invokeOnCancellation { }
                    }

                location?.let {
                    val currentLatLng = com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude)
                    cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(currentLatLng, 18f)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    com.google.maps.android.compose.GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties =
            com.google.maps.android.compose.MapProperties(
                isMyLocationEnabled = hasLocationPermission,
            ),
    )
}

@Composable
fun ExpandedStatsScreen(
    metrics: ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics,
    detailedStats: Map<String, Any>,
    uiState: TrackingUiState,
    onCollapseStats: () -> Unit,
    onStopClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Panel expandido de estadísticas
        TrackingPanel(
            metrics = metrics,
            uiState = uiState,
            detailedStats = detailedStats,
            onPauseClick = {},
            onResumeClick = {},
            onStopClick = onStopClick,
            onExpandClick = onCollapseStats,
            isExpanded = true,
        )
    }
}

@Composable
fun TrackingPanel(
    metrics: ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics,
    uiState: TrackingUiState,
    detailedStats: Map<String, Any> = emptyMap(),
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit,
    onExpandClick: () -> Unit,
    isExpanded: Boolean,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header con botón expandir/contraer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (isExpanded) "Estadísticas Completas" else "Métricas Básicas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onExpandClick) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Contraer" else "Expandir",
                    )
                }
            }

            if (isExpanded) {
                // Estadísticas completas de todos los sensores
                ExpandedStatsContent(
                    metrics = metrics,
                    detailedStats = detailedStats,
                )
            } else {
                // Métricas básicas
                BasicMetricsContent(metrics = metrics)
            }

            // Botones de control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Botón Pause/Resume
                if (uiState.canPause) {
                    Button(
                        onClick = onPauseClick,
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                            ),
                    ) {
                        Text("⏸️")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pausar")
                    }
                } else if (uiState.canResume) {
                    Button(
                        onClick = onResumeClick,
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reanudar")
                    }
                }

                // Botón Stop
                if (uiState.canStop) {
                    Button(
                        onClick = onStopClick,
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                            ),
                    ) {
                        Text("⏹️")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Detener")
                    }
                }
            }
        }
    }
}

@Composable
fun BasicMetricsContent(metrics: ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        MetricCard(
            title = "Distancia",
            value = "%.2f km".format(metrics.currentDistance),
            modifier = Modifier.weight(1f),
        )
        MetricCard(
            title = "Velocidad",
            value = "%.1f km/h".format(metrics.currentSpeed),
            modifier = Modifier.weight(1f),
        )
        MetricCard(
            title = "Elevación",
            value = "%.0f m".format(metrics.currentElevation),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun ExpandedStatsContent(
    metrics: ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics,
    detailedStats: Map<String, Any>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // GPS Metrics
        Text(
            text = "📍 GPS y Ubicación",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard(
                title = "Distancia",
                value = "%.2f km".format(metrics.currentDistance),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "Velocidad",
                value = "%.1f km/h".format(metrics.currentSpeed),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "Vel. Máx.",
                value = "%.1f km/h".format(metrics.maxSpeed),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Barometer Metrics
        Text(
            text = "⛰️ Barómetro y Elevación",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard(
                title = "Elevación",
                value = "%.0f m".format(metrics.currentElevation),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "Ganancia",
                value = "+%.0f m".format(metrics.totalElevationGain),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "Pérdida",
                value = "-%.0f m".format(metrics.totalElevationLoss),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Additional Stats from Accelerometer and other sensors
        if (detailedStats.isNotEmpty()) {
            Text(
                text = "📊 Métricas Adicionales",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard(
                    title = "Tiempo",
                    value = detailedStats["elapsedTimeFormatted"]?.toString() ?: "00:00:00",
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    title = "Puntos GPS",
                    value = detailedStats["pointsCount"]?.toString() ?: "0",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
