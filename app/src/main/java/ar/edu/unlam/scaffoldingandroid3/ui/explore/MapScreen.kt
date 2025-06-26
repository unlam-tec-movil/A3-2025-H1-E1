package ar.edu.unlam.scaffoldingandroid3.ui.explore

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.ui.navigation.Screen
import ar.edu.unlam.scaffoldingandroid3.ui.shared.ErrorDialog
import ar.edu.unlam.scaffoldingandroid3.ui.shared.LoadingSpinner
import ar.edu.unlam.scaffoldingandroid3.ui.shared.bitmapFromVector
import ar.edu.unlam.scaffoldingandroid3.ui.theme.dimens
import com.google.android.gms.maps.model.MapStyleOptions

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
 * @param navController controla la navegación hacia otras pantallas
 */
@Composable
fun MapScreen(
    navController: NavHostController,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                viewModel.onPermissionResult(isGranted)
            },
        )

    val snackbarHostState = remember { SnackbarHostState() }

    // Hoisted, unconditional remembers
    val markerBitmap = remember { bitmapFromVector(context, R.drawable.ic_marker_background) }
    val hikerBitmap = remember { bitmapFromVector(context, R.drawable.ic_hiking) }
    val mapStyleOptions =
        remember {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
        }

    LaunchedEffect(uiState.showNoResultsMessage) {
        if (uiState.showNoResultsMessage) {
            snackbarHostState.showSnackbar(context.getString(R.string.map_no_results_found))
            viewModel.onMessageShown()
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.onPermissionResult(true)
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            val mapAlpha by animateFloatAsState(
                targetValue = if (uiState.currentLocation != null) 1f else 0f,
                label = "Map Alpha Animation",
            )

            // MapContent is the "dumb" composable that just displays the map and markers
            MapContent(
                modifier =
                    Modifier
                        .matchParentSize()
                        .alpha(mapAlpha),
                uiState = uiState,
                cameraPositionState = uiState.cameraPositionState,
                mapStyleOptions = mapStyleOptions,
                onRouteClick = { selectedRoute ->
                    navController.navigate(Screen.RouteDetail.route)
                    navController.getBackStackEntry(Screen.RouteDetail.route).savedStateHandle["route"] =
                        selectedRoute
                },
                onMapIdle = viewModel::onMapIdle,
                markerBitmap = markerBitmap,
                hikerBitmap = hikerBitmap,
            )

            // The loading spinner is now just an overlay
            if (uiState.isLoading) {
                LoadingSpinner()
            }

            AnimatedVisibility(
                visible = uiState.showSearchInAreaButton,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = MaterialTheme.dimens.paddingMedium),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Button(
                    onClick = {
                        uiState.cameraPositionState.position.target.let { newCenter ->
                            viewModel.searchInMapArea(newCenter)
                        }
                    },
                    shape = RoundedCornerShape(MaterialTheme.dimens.cornerRadiusMedium),
                ) {
                    Text(text = stringResource(id = R.string.map_search_in_area))
                }
            }

            uiState.error?.let {
                ErrorDialog(
                    errorMessage = it,
                    onDismiss = { viewModel.clearError() },
                )
            }

            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.inversePrimary,
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.dimens.paddingMedium),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.paddingSmall),
                ) {
                    Button(
                        onClick = { navController.navigate(Screen.Tracking.route) },
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(MaterialTheme.dimens.buttonHeightNormal),
                    ) {
                        Text(text = stringResource(id = R.string.new_route))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MapScreenPreview() {
    MaterialTheme {
        MapScreen(
            navController = NavHostController(LocalContext.current),
        )
    }
}
