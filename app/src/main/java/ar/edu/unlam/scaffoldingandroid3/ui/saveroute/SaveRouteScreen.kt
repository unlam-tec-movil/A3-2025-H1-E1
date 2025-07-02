package ar.edu.unlam.scaffoldingandroid3.ui.saveroute

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingResult
import ar.edu.unlam.scaffoldingandroid3.ui.shared.ConfettiAnimation
import ar.edu.unlam.scaffoldingandroid3.ui.shared.RouteImage
import coil.compose.AsyncImage

/**
 * Pantalla para guardar el recorrido completado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveRouteScreen(
    onNavigateBack: () -> Unit,
    onSaveRoute: (String) -> Unit,
    onDiscardRoute: () -> Unit,
    viewModel: SaveRouteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val trackingResult by viewModel.trackingResult.collectAsStateWithLifecycle()

    // Manejar botón back del sistema Android
    BackHandler {
        viewModel.discardRoute()
        onNavigateBack()
    }

    // Dialogs
    if (uiState.showDiscardDialog) {
        DiscardRouteDialog(
            onConfirm = {
                viewModel.hideDiscardDialog()
                viewModel.discardRoute()
                onDiscardRoute()
            },
            onDismiss = { viewModel.hideDiscardDialog() },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                // Sin título
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.discardRoute()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showDiscardDialog() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Estadísticas del recorrido con animación
                trackingResult?.let { result ->
                    AnimatedStatsSection(result)

                    // Campo nombre con animación
                    AnimatedNameInputSection(
                        routeName = uiState.routeName,
                        onNameChange = { viewModel.updateRouteName(it) },
                        isError = uiState.nameError != null,
                        errorMessage = uiState.nameError,
                    )

                    // Sección de fotos con animación
                    AnimatedPhotosSection(
                        photos = result.foto,
                    )
                } ?: run {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                // Botón hacia abajo
                Box(modifier = Modifier.weight(1f))

                // Botón guardar
                Button(
                    onClick = {
                        if (viewModel.validateAndSave()) {
                            viewModel.saveTrackingResult(
                                onSuccess = { onSaveRoute(uiState.routeName) },
                                onError = { /* TODO: Mostrar error en Snackbar */ },
                            )
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    enabled = !uiState.isLoading,
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Guardar recorrido", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            // Confetti de celebración - pasa por encima de todo el contenido
            ConfettiAnimation(
                modifier = Modifier.fillMaxSize(),
                particleCount = 80,
                // Duración
                duration = 4000,
            )
        }
    }
}

@Composable
private fun StatsSection(trackingResult: TrackingResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Estadísticas del recorrido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            StatRow("Duración:", trackingResult.duracion)
            StatRow("Pasos:", "${trackingResult.pasosTotales}")
            StatRow("Distancia recorrida:", formatDistance(trackingResult.distanciaTotal))
            StatRow("Velocidad Media:", "%.1f km/h".format(trackingResult.velocidadMedia))
            StatRow("Velocidad Máxima:", "%.1f km/h".format(trackingResult.velocidadMaxima))
            StatRow("Altitud mínima:", "%.0f m".format(trackingResult.altitudMinima))
            StatRow("Altitud máxima:", "%.0f m".format(trackingResult.altitudMaxima))
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun NameInputSection(
    routeName: String,
    onNameChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Nombre del recorrido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = routeName,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ingresa nombre para esta caminata") },
                isError = isError,
                supportingText =
                    if (errorMessage != null) {
                        { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                    } else {
                        null
                    },
                singleLine = true,
            )
        }
    }
}

@Composable
private fun PhotosSection(photos: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (photos.isNotEmpty()) {
                RouteImage(modifier = Modifier, photos)
            } else {
                Text(
                    text = "No hay fotos capturadas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(photoUri: String) {
    Card(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
        AsyncImage(
            model = photoUri,
            contentDescription = "Foto del recorrido",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun DiscardRouteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Eliminar este recorrido?") },
        text = { Text("Se perderán todos los datos del recorrido actual.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
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
 * Sección de estadísticas con animación mejorada
 */
@Composable
private fun AnimatedStatsSection(trackingResult: TrackingResult) {
    val visible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(800)
        visible.value = true
    }

    AnimatedVisibility(
        visible = visible.value,
        enter =
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
            ) +
                scaleIn(
                    initialScale = 0.7f,
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                ) +
                fadeIn(
                    animationSpec = tween(800),
                ),
    ) {
        StatsSection(trackingResult)
    }
}

/**
 * Sección de nombre con animación mejorada
 */
@Composable
private fun AnimatedNameInputSection(
    routeName: String,
    onNameChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String?,
) {
    val visible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1400) // Delay más escalonado
        visible.value = true
    }

    AnimatedVisibility(
        visible = visible.value,
        enter =
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
            ) +
                scaleIn(
                    initialScale = 0.6f,
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                ) +
                fadeIn(
                    animationSpec = tween(900),
                ),
    ) {
        NameInputSection(
            routeName = routeName,
            onNameChange = onNameChange,
            isError = isError,
            errorMessage = errorMessage,
        )
    }
}

/**
 * Sección de fotos con animación espectacular
 */
@Composable
private fun AnimatedPhotosSection(photos: String) {
    val visible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2100) // Delay más largo
        visible.value = true
    }

    AnimatedVisibility(
        visible = visible.value,
        enter =
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessVeryLow,
                    ),
            ) +
                scaleIn(
                    initialScale = 0.5f,
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessVeryLow,
                        ),
                ) +
                fadeIn(
                    animationSpec = tween(1000),
                ),
    ) {
        PhotosSection(
            photos = photos,
        )
    }
}

/**
 * Formatea distancia siempre en metros
 */
private fun formatDistance(distanceKm: Double): String {
    val meters = (distanceKm * 1000).toInt()
    return "$meters m"
}
