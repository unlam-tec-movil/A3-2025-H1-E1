package ar.edu.unlam.scaffoldingandroid3.ui.explore

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
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
 * @param selectedRoute la ruta seleccionada para mostrar
 */

@Composable
fun MapScreen(
    navController: NavHostController,
    selectedRoute: Route? = null,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                viewModel.onPermissionResult(isGranted)
            },
        )

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && photoUri != null) {
                viewModel.onPhotoTaken(photoUri!!) // Pasás el URI al ViewModel
            }
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraLauncher.launch(intent)
                }
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

    // Si llega una ruta externa, pedir al ViewModel que la muestre
    LaunchedEffect(selectedRoute) {
        selectedRoute?.let { viewModel.showRoute(it) }
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

            FloatingActionButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA,
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val photoFile = viewModel.createImageFile(context)
                        photoUri =
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                photoFile,
                            )
                        val intent =
                            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                            }
                        cameraLauncher.launch(intent)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(
                            bottom = 120.dp,
                            start = MaterialTheme.dimens.paddingMedium,
                        )
                        .size(56.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = stringResource(R.string.camera_button_content_description),
                    modifier = Modifier.size(24.dp),
                )
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
                    if (uiState.selectedRoute != null) {
                        Button(
                            onClick = {
                                viewModel.clearSelectedRoute()
                                navController.navigate(Screen.Tracking.route)
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(MaterialTheme.dimens.buttonHeightNormal),
                        ) {
                            Text(text = stringResource(id = R.string.start_route))
                        }
                    } else {
                        Button(
                            onClick = { navController.navigate(Screen.Tracking.route) },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(MaterialTheme.dimens.buttonHeightNormal),
                        ) {
                            Text(text = stringResource(id = R.string.new_route))
                        }
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
