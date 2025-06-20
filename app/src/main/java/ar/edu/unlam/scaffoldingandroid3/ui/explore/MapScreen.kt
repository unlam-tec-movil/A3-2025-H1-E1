package ar.edu.unlam.scaffoldingandroid3.ui.explore

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.ui.shared.ErrorDialog
import ar.edu.unlam.scaffoldingandroid3.ui.shared.LoadingSpinner
import ar.edu.unlam.scaffoldingandroid3.ui.shared.bitmapFromVector
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings

/**
 * Pantalla principal del mapa que muestra la ubicación actual y permite la interacción con rutas.
 *
 * Esta pantalla es parte de la capa de UI y actúa como contenedor principal para la funcionalidad
 * del mapa. Maneja:
 * - Permisos de ubicación
 * - Visualización del mapa de Google
 * - Botones de acción para grabar y cargar rutas
 * - Navegación inferior
 *
 * @param onNewRouteClick Callback para iniciar la grabación de una nueva ruta
 * @param onLoadRoutesClick Callback para cargar rutas existentes
 * @param onRouteClick Callback para navegar a la pantalla de detalle de ruta
 */
@Composable
fun MapScreen(
    onNewRouteClick: () -> Unit,
    onLoadRoutesClick: () -> Unit,
    onRouteClick: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.onPermissionResult(isGranted)
        }
    )

    val snackbarHostState = remember { SnackbarHostState() }

    // Hoisted, unconditional remembers
    val markerBitmap = remember { bitmapFromVector(context, R.drawable.ic_marker_background) }
    val hikerBitmap = remember { bitmapFromVector(context, R.drawable.ic_hiking) }
    val mapStyleOptions = remember { MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style) }

    LaunchedEffect(uiState.showNoResultsMessage) {
        if (uiState.showNoResultsMessage) {
            snackbarHostState.showSnackbar(context.getString(R.string.map_no_results_found))
            viewModel.onMessageShown()
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.onPermissionResult(true)
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val mapAlpha by animateFloatAsState(
                targetValue = if (uiState.currentLocation != null) 1f else 0f,
                label = "Map Alpha Animation"
            )

            // GoogleMap is always in the composition tree
            GoogleMap(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(mapAlpha),
                cameraPositionState = uiState.cameraPositionState, // From ViewModel
                contentPadding = PaddingValues(bottom = 80.dp),
                properties = MapProperties(
                    isMyLocationEnabled = uiState.isLocationEnabled,
                    mapStyleOptions = mapStyleOptions
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = uiState.isLocationEnabled,
                    zoomControlsEnabled = true,
                )
            )

            // This block now only depends on hoisted variables or state from the ViewModel
            if (uiState.currentLocation != null) {
                LaunchedEffect(uiState.cameraPositionState) {
                    snapshotFlow { uiState.cameraPositionState.isMoving }
                        .collect { isMoving ->
                            if (isMoving == false) {
                                uiState.cameraPositionState.position.let { viewModel.onMapIdle(it) }
                            }
                        }
                }

                val density = LocalDensity.current

                uiState.nearbyRoutes.forEach { route ->
                    route.points.firstOrNull()?.let { startPoint ->
                        val routeLatLng = LatLng(startPoint.latitude, startPoint.longitude)

                        val screenPos = uiState.cameraPositionState.projection?.toScreenLocation(routeLatLng)

                        if (screenPos != null && markerBitmap != null && hikerBitmap != null) {
                            val xDp = with(density) { (screenPos.x - markerBitmap.width / 2).toDp() }
                            val yDp = with(density) { (screenPos.y - markerBitmap.height).toDp() }
                            Box(
                                modifier = Modifier.offset(x = xDp, y = yDp)
                            ) {
                                CustomMarkerView(
                                    routeName = route.name,
                                    showLabel = uiState.cameraPositionState.position.zoom > 11f,
                                    markerBitmap = markerBitmap,
                                    hikerBitmap = hikerBitmap,
                                    onIconClick = { onRouteClick(route.id) }
                                )
                            }
                        }
                    }
                }
            }

            // The loading spinner is now just an overlay
            if (uiState.isLoading) {
                LoadingSpinner()
            }

            AnimatedVisibility(
                visible = uiState.showSearchInAreaButton,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Button(
                    onClick = {
                        uiState.cameraPositionState.position.target.let { newCenter ->
                            viewModel.searchInMapArea(newCenter)
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = stringResource(id = R.string.map_search_in_area))
                }
            }

            uiState.error?.let {
                ErrorDialog(
                    errorMessage = it,
                    onDismiss = { viewModel.clearError() }
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = Color.White,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onLoadRoutesClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    ) {
                        Text(text = stringResource(id = R.string.load_routes))
                    }

                    Button(
                        onClick = onNewRouteClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    ) {
                        Text(text = stringResource(R.string.record_route))
                    }
                }
            }
        }
    }
}

@Composable
fun CustomMarkerView(
    routeName: String,
    showLabel: Boolean,
    markerBitmap: android.graphics.Bitmap,
    hikerBitmap: android.graphics.Bitmap,
    onIconClick: () -> Unit
) {
    Box(contentAlignment = Alignment.TopCenter) {
        Image(
            bitmap = markerBitmap.asImageBitmap(),
            contentDescription = stringResource(id = R.string.map_route_pin_content_description),
            modifier = Modifier.size(48.dp)
        )
        Image(
            bitmap = hikerBitmap.asImageBitmap(),
            contentDescription = stringResource(id = R.string.map_hiker_icon_content_description),
            modifier = Modifier
                .size(24.dp)
                .padding(top = 6.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // Sin efecto de ripple
                    onClick = onIconClick
                )
        )
        if (showLabel) {
            // Usamos un offset para que el label aparezca debajo del pin
            Box(
                modifier = Modifier
                    .padding(top = 48.dp)
                    .widthIn(max = 144.dp) // Limita el ancho del texto a 3x el marcador
            ) {
                RouteLabel(routeName = routeName)
            }
        }
    }
}

@Composable
fun RouteLabel(routeName: String) {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = routeName,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.7f),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            ),
            color = Color.Black,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MapPreview() {
    MapScreen(onNewRouteClick = {}, onLoadRoutesClick = {}, onRouteClick = {})
}
