package ar.edu.unlam.scaffoldingandroid3.ui.saveroute

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingResult

/**
 * Pantalla para guardar el recorrido completado
 * Compatible con TRACKING_REQUIREMENTS.md líneas 207-265
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveRouteScreen(
    trackingResult: TrackingResult,
    onNavigateBack: () -> Unit,
    onSaveRoute: (String) -> Unit,
    onDiscardRoute: () -> Unit,
    viewModel: SaveRouteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Dialogs
    if (uiState.showDiscardDialog) {
        DiscardRouteDialog(
            onConfirm = {
                viewModel.hideDiscardDialog()
                onDiscardRoute()
            },
            onDismiss = { viewModel.hideDiscardDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guardar recorrido") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showDiscardDialog() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estadísticas del recorrido
            StatsSection(trackingResult)
            
            // Campo nombre
            NameInputSection(
                routeName = uiState.routeName,
                onNameChange = { viewModel.updateRouteName(it) },
                isError = uiState.nameError != null,
                errorMessage = uiState.nameError
            )
            
            // Sección de fotos
            PhotosSection(
                photos = trackingResult.fotosCapturadas,
                onAddPhoto = { /* TODO: Implementar agregar foto */ }
            )
            
            // Botón guardar
            Button(
                onClick = {
                    if (viewModel.validateAndSave()) {
                        viewModel.saveTrackingResult(
                            trackingResult = trackingResult,
                            onSuccess = { onSaveRoute(uiState.routeName) },
                            onError = { /* TODO: Mostrar error en Snackbar */ }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Guardar recorrido", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun StatsSection(trackingResult: TrackingResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Estadísticas del recorrido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            StatRow("Tiempo de grabación:", trackingResult.tiempoTotal)
            StatRow("Tiempo en movimiento:", trackingResult.tiempoEnMovimiento)
            StatRow("Pasos:", "${trackingResult.pasosTotales}")
            StatRow("Distancia recorrida:", "%.2f km".format(trackingResult.distanciaTotal))
            StatRow("Velocidad Media:", "%.1f km/h".format(trackingResult.velocidadMedia))
            StatRow("Velocidad Máxima:", "%.1f km/h".format(trackingResult.velocidadMaxima))
            StatRow("Altitud mínima:", "%.0f m".format(trackingResult.altitudMinima))
            StatRow("Altitud máxima:", "%.0f m".format(trackingResult.altitudMaxima))
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NameInputSection(
    routeName: String,
    onNameChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Nombre del recorrido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = routeName,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ingresa nombre para esta caminata") },
                isError = isError,
                supportingText = if (errorMessage != null) {
                    { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                } else null,
                singleLine = true
            )
        }
    }
}

@Composable
private fun PhotosSection(
    photos: List<ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingPhoto>,
    onAddPhoto: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fotos: ${photos.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onAddPhoto) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (photos.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(photos) { photo ->
                        PhotoThumbnail(photo)
                    }
                }
            } else {
                Text(
                    text = "No hay fotos capturadas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(photo: ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingPhoto) {
    Card(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // TODO: Cargar imagen real usando Coil o similar
            Text(
                text = "📷",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
private fun DiscardRouteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
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
        }
    )
}