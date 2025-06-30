package ar.edu.unlam.scaffoldingandroid3.ui.tracking

import android.Manifest
import android.net.Uri
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.FileProvider
import android.provider.MediaStore
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingResult
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Pantalla de tracking con 3 estados y integración completa de sensores
 * PREPARATION → RECORDING → EXPANDED_STATS
 * Integra: GPS, Acelerómetro, Barómetro, Magnetómetro
 */
@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalPermissionsApi::class)
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

    // Crear el estado del permiso que queremos solicitar
    val activityRecognitionPermissionState =
        rememberPermissionState(
            android.Manifest.permission.ACTIVITY_RECOGNITION,
        )

    // Usar LaunchedEffect para solicitar el permiso una vez cuando la pantalla se carga
    LaunchedEffect(Unit) {
        if (!activityRecognitionPermissionState.status.isGranted) {
            activityRecognitionPermissionState.launchPermissionRequest()
        }
    }

    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && photoUri != null) {
            viewModel.onPhotoTaken(photoUri!!)
        }
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    val photoFile = viewModel.createImageFile(context)
                    photoUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    }
                    cameraLauncher.launch(intent)
                }
            },
        )

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
            onDismiss = { viewModel.hideDiscardDialog() },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                }
            },
            isPermissionGranted = activityRecognitionPermissionState.status.isGranted,
            onExpandStats = viewModel::toggleStatsExpansion,
            onCapturePhoto =  {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                val photoFile = viewModel.createImageFile(context)
                photoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                }
                cameraLauncher.launch(intent)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        )

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
    isPermissionGranted: Boolean,
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
            showZoomControls = false,
        )

        FloatingNavigationButton(
            onNavigationBack = onNavigationBack,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
        )
        // Botón de cámara
        if (uiState.screenState == TrackingScreenState.RECORDING ||
            uiState.screenState == TrackingScreenState.EXPANDED_STATS
        ) {
            Row(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 72.dp)
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
            color = Color.Transparent,
            shadowElevation = 0.dp,
        ) {
            when (uiState.screenState) {
                TrackingScreenState.PREPARATION -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Solo botón "Iniciar recorrido"
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Button(
                                onClick = onStartClick,
                                enabled = !uiState.isLoading && isPermissionGranted,
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
                        // Mostrar un texto de ayuda si el permiso fue denegado.
                        if (!isPermissionGranted) {
                            Text(
                                text =
                                    "Se necesita permiso de 'Actividad Física' para contar los pasos. " +
                                        "La app funcionará, pero los pasos no se medirán.",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                TrackingScreenState.RECORDING, TrackingScreenState.EXPANDED_STATS -> {
                    // UN SOLO PANEL que se mantiene durante ambos estados
                    TrackingPanel(
                        metrics = metrics,
                        uiState = uiState,
                        detailedStats = detailedStats,
                        onPauseClick = onPauseClick,
                        onResumeClick = onResumeClick,
                        onStopClick = onStopClick,
                        onExpandClick = onExpandStats,
                        isExpanded = uiState.screenState == TrackingScreenState.EXPANDED_STATS,
                    )
                }
            }
        }

        // Botón flechita solo visible cuando hay panel de estadísticas
        if (uiState.screenState != TrackingScreenState.PREPARATION) {
            FixedArrowButton(
                isExpanded = uiState.screenState == TrackingScreenState.EXPANDED_STATS,
                onClick = onExpandStats,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = 120.dp),
            )
        }
    }
}

@Composable
fun ActiveTrackingMap(
    modifier: Modifier = Modifier,
    routePoints: List<com.google.android.gms.maps.model.LatLng> = emptyList(),
    currentLocation: com.google.android.gms.maps.model.LatLng? = null,
    showZoomControls: Boolean = true,
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
        uiSettings =
            com.google.maps.android.compose.MapUiSettings(
                zoomControlsEnabled = showZoomControls,
            ),
    ) {
        // Dibujar polyline de la ruta en tiempo real
        if (routePoints.size >= 2) {
            com.google.maps.android.compose.Polyline(
                points = routePoints,
                color = Color(0xFF2196F3),
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
    // Control de aparición inicial del panel (solo una vez)
    var hasAppeared by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        hasAppeared = true
    }

    // Animación de aparición inicial del panel COMPLETO (solo cuando aparece por primera vez)
    AnimatedVisibility(
        visible = hasAppeared,
        enter =
            slideInVertically(
                // Desde abajo
                initialOffsetY = { it },
                animationSpec =
                    tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing,
                    ),
            ) + fadeIn(animationSpec = tween(500)),
    ) {
        TrackingPanelContent(
            metrics = metrics,
            uiState = uiState,
            detailedStats = detailedStats,
            onPauseClick = onPauseClick,
            onResumeClick = onResumeClick,
            onStopClick = onStopClick,
            onExpandClick = onExpandClick,
            isExpanded = isExpanded,
        )
    }
}

@Composable
private fun TrackingPanelContent(
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
                        },
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
        // Sin sombra
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        // Fondo transparente
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Estadísticas adicionales con efecto cajón suave
                AnimatedVisibility(
                    visible = isExpanded,
                    enter =
                        slideInVertically(
                            // Entra desde arriba
                            initialOffsetY = { -it },
                            animationSpec = tween(400, easing = FastOutSlowInEasing),
                        ) + fadeIn(animationSpec = tween(400)),
                    exit =
                        slideOutVertically(
                            // Sale hacia ABAJO con caída suave
                            targetOffsetY = { it },
                            animationSpec =
                                tween(
                                    // Ligeramente más lento
                                    durationMillis = 450,
                                    // Ease out suave
                                    easing =
                                        androidx.compose.animation.core.CubicBezierEasing(
                                            0.25f,
                                            0.46f,
                                            0.45f,
                                            0.94f,
                                        ),
                                ),
                        ) + fadeOut(animationSpec = tween(400)),
                ) {
                    // Estadísticas adicionales (Velocidad, Altitud)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                }

                // Estadísticas base siempre visibles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MetricCard(
                        title = "Duración",
                        value = uiState.elapsedTime,
                        modifier = Modifier.weight(1f),
                    )
                    MetricCard(
                        title = "Distancia",
                        value = formatDistance(metrics.currentDistance),
                        modifier = Modifier.weight(1f),
                    )
                    MetricCard(
                        title = "Pasos",
                        value = uiState.stepCount.toString(),
                        modifier = Modifier.weight(1f),
                    )
                }

                // Errores de sensores (solo si hay errores y está expandido)
                if (uiState.sensorErrors.isNotEmpty() && isExpanded) {
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn(animationSpec = tween(400)),
                        exit = fadeOut(animationSpec = tween(350)),
                    ) {
                        Column {
                            Text(
                                text = "⚠️ Estado de Sensores",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error,
                            )
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

                // Botones de control siempre visibles
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
private fun FloatingNavigationButton(
    onNavigationBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onNavigationBack,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shadowElevation = 4.dp,
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun DiscardTrackingDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
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
        },
    )
}

/**
 * Formatea distancia dinámicamente: metros si < 1km, kilómetros si >= 1km
 * Comportamiento profesional como Strava/Nike Run
 */
private fun formatDistance(distanceKm: Double): String {
    return if (distanceKm < 1.0) {
        // Mostrar en metros para distancias pequeñas
        val meters = (distanceKm * 1000).toInt()
        "$meters m"
    } else {
        // Mostrar en kilómetros para distancias grandes
        "%.2f km".format(distanceKm)
    }
}

/**
 * Botón flechita con posición fija (no se mueve con el panel)
 */
@Composable
private fun FixedArrowButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Animación simple de rotación con spring elegante
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "fixed_arrow_rotation",
    )

    Surface(
        onClick = onClick,
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF2196F3).copy(alpha = 0.9f),
        shadowElevation = 3.dp,
        tonalElevation = 1.dp,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = if (isExpanded) "Contraer" else "Expandir",
                tint = Color.White,
                modifier =
                    Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            rotationZ = arrowRotation // Rotación suave con spring
                        },
            )
        }
    }
}
