package ar.edu.unlam.scaffoldingandroid3.ui.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingResult
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
    onTrackingCompleted: (TrackingResult) -> Unit = {},
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
    
    // Dialog de confirmación para descartar tracking
    if (uiState.showDiscardDialog) {
        DiscardTrackingDialog(
            onConfirm = {
                viewModel.hideDiscardDialog()
                viewModel.discardTracking()
                onNavigationBack()
            },
            onDismiss = { viewModel.hideDiscardDialog() }
        )
    }

    // Sin Scaffold ni AppBar - diseño completamente minimalista
    Box(modifier = Modifier.fillMaxSize()) {
            // Pantalla única con mapa y botones dinámicos
            TrackingMapScreen(
                metrics = metrics,
                detailedStats = detailedStats,
                uiState = uiState,
                onNavigationBack = onNavigationBack,
                onStartClick = { viewModel.startTracking("Ruta ${System.currentTimeMillis()}") },
                onPauseClick = viewModel::pauseTracking,
                onResumeClick = viewModel::resumeTracking,
                onStopClick = {
                    viewModel.stopTracking { trackingResult ->
                        onTrackingCompleted(trackingResult)
                        onNavigationBack()
                    }
                },
                onExpandStats = viewModel::toggleStatsExpansion,
                onCapturePhoto = viewModel::capturePhoto,
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

@Composable
fun TrackingMapScreen(
    metrics: ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics,
    detailedStats: Map<String, Any>,
    uiState: TrackingUiState,
    onNavigationBack: () -> Unit,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit,
    onExpandStats: () -> Unit,
    onCapturePhoto: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa de fondo con ruta en tiempo real
        ActiveTrackingMap(
            modifier = Modifier.fillMaxSize(),
            routePoints = uiState.routePoints,
            currentLocation = uiState.currentLocation,
        )

        // Botón de navegación flotante minimalista (siempre visible)
        FloatingNavigationButton(
            onNavigationBack = onNavigationBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        // Botón de cámara debajo de la flecha de navegación (solo durante grabación)
        if (uiState.screenState == TrackingScreenState.RECORDING ||
            uiState.screenState == TrackingScreenState.EXPANDED_STATS
        ) {
            Row(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 72.dp) // Debajo del botón de navegación
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(20.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(
                    onClick = onCapturePhoto,
                ) {
                    Text(
                        text = "📷",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "[${uiState.photoCount}]",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

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
fun ActiveTrackingMap(
    modifier: Modifier = Modifier,
    routePoints: List<com.google.android.gms.maps.model.LatLng> = emptyList(),
    currentLocation: com.google.android.gms.maps.model.LatLng? = null,
) {
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
            position =
                com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                    currentLocation ?: defaultLocation,
                    if (currentLocation != null) 18f else 15f,
                )
        }

    // Obtener la ubicación actual si no la tenemos
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && currentLocation == null) {
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

    // Actualizar cámara cuando cambie la ubicación actual
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(it, 18f)
        }
    }

    com.google.maps.android.compose.GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties =
            com.google.maps.android.compose.MapProperties(
                isMyLocationEnabled = hasLocationPermission,
            ),
    ) {
        // Dibujar polyline de la ruta en tiempo real
        if (routePoints.size >= 2) {
            com.google.maps.android.compose.Polyline(
                points = routePoints,
                color = androidx.compose.ui.graphics.Color(0xFF2196F3),
                width = 8f,
            )
        }

        // Marcador en la ubicación actual
        currentLocation?.let { location ->
            com.google.maps.android.compose.Marker(
                state = com.google.maps.android.compose.rememberMarkerState(position = location),
                title = "Ubicación actual",
            )
        }
    }
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
                .padding(8.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            // Al terminar el drag, cambiar estado
                            onExpandClick()
                        }
                    ) { change, dragAmount ->
                        // Detectar dirección del drag
                        val threshold = 50f
                        if (kotlin.math.abs(dragAmount.y) > threshold) {
                            if (!isExpanded && dragAmount.y < -threshold) {
                                // Deslizar hacia arriba para expandir
                                onExpandClick()
                            } else if (isExpanded && dragAmount.y > threshold) {
                                // Deslizar hacia abajo para colapsar
                                onExpandClick()
                            }
                        }
                    }
                },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Box {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(if (isExpanded) 12.dp else 6.dp),
            ) {
                if (isExpanded) {
                    // Estadísticas completas de todos los sensores
                    ExpandedStatsContent(
                        metrics = metrics,
                        detailedStats = detailedStats,
                        uiState = uiState,
                    )
                } else {
                    // Métricas básicas compactas
                    CompactMetricsContent(
                        metrics = metrics,
                        elapsedTime = uiState.elapsedTime,
                        stepCount = uiState.stepCount,
                    )
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
            
            // Flecha expandir/contraer en esquina superior derecha
            IconButton(
                onClick = onExpandClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isExpanded) "Contraer" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun CompactMetricsContent(
    metrics: ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics,
    elapsedTime: String,
    stepCount: Int = 0,
) {
    // Panel plegado: compacto y con altura mínima
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CompactMetricCard(
            title = "Tiempo Activo",
            value = elapsedTime,
            modifier = Modifier.weight(1f),
        )
        CompactMetricCard(
            title = "Distancia", 
            value = "%.2f km".format(metrics.currentDistance),
            modifier = Modifier.weight(1f),
        )
        CompactMetricCard(
            title = "Pasos",
            value = stepCount.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun BasicMetricsContent(
    metrics: ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics,
    elapsedTime: String,
    stepCount: Int = 0,
) {
    // Fila única con las 3 métricas principales (más grandes y sin título del panel)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EnhancedMetricCard(
            title = "Tiempo Activo",
            value = elapsedTime,
            modifier = Modifier.weight(1f),
        )
        EnhancedMetricCard(
            title = "Distancia",
            value = "%.2f km".format(metrics.currentDistance),
            modifier = Modifier.weight(1f),
        )
        EnhancedMetricCard(
            title = "Pasos",
            value = stepCount.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun ExpandedStatsContent(
    metrics: ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics,
    detailedStats: Map<String, Any>,
    uiState: TrackingUiState,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Primera fila: Vel. Actual, Vel. Máx, Altitud
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Vel. Actual",
                value = "%.1f km/h".format(metrics.currentSpeed),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "Vel. Máx.",
                value = "%.1f km/h".format(metrics.maxSpeed),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "Altitud",
                value = "%.0f m".format(metrics.currentElevation),
                modifier = Modifier.weight(1f),
            )
        }

        // Segunda fila: Tiempo Activo, Distancia, Pasos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Tiempo Activo",
                value = uiState.elapsedTime,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "Distancia",
                value = "%.2f km".format(metrics.currentDistance),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = "Pasos",
                value = uiState.stepCount.toString(),
                modifier = Modifier.weight(1f),
            )
        }

        // Mostrar errores de sensores si existen
        if (uiState.sensorErrors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "⚠️ Estado de Sensores",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error,
            )
            Column {
                uiState.sensorErrors.forEach { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
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

@Composable
fun CompactMetricCard(
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
            modifier = Modifier.padding(6.dp), // Padding compacto
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall, // Título pequeño
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium, // Valor moderado
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun EnhancedMetricCard(
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
            modifier = Modifier.padding(12.dp), // Más padding que la card normal
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium, // Título ligeramente más grande
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall, // Valor mucho más grande
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun FloatingNavigationButton(
    onNavigationBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onNavigationBack,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp) // Flecha más grande
            )
        }
    }
}

@Composable
private fun DiscardTrackingDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Descartar recorrido?") },
        text = { Text("Se perderán todos los datos del recorrido actual.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Descartar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

