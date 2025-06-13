package ar.edu.unlam.scaffoldingandroid3.ui.explore

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import ar.edu.unlam.scaffoldingandroid3.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
 */
@Composable
fun MapScreen(
    onNewRouteClick: () -> Unit,
    onLoadRoutesClick: () -> Unit,
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            hasLocationPermission = isGranted
        }

    // Ubicación inicial (Buenos Aires)
    val defaultLocation = LatLng(-34.6037, -58.3816)
    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
        }

    // Obtener la ubicación actual
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val fusedLocationClient: FusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(context)

                val location =
                    suspendCancellableCoroutine<Location?> { continuation ->
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location ->
                                continuation.resume(location)
                            }
                            .addOnFailureListener { e ->
                                continuation.resumeWithException(e)
                            }
                    }

                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
                }
            } catch (e: Exception) {
                // Si hay algún error, mantenemos la ubicación por defecto
                e.printStackTrace()
            }
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa que se ajusta al espacio disponible
        GoogleMap(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(bottom = 144.dp),
            // Espacio para los botones y el navigation bar
            cameraPositionState = cameraPositionState,
            properties =
                MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                ),
        )

        // Contenedor de botones superpuesto con fondo blanco
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
            // Espacio para el navigation bar
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Botón "Cargar ruta"
                Button(
                    onClick = onLoadRoutesClick,
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(56.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                ) {
                    Text(text = "Cargar ruta")
                }

                // Botón "Grabar ruta"
                Button(
                    onClick = onNewRouteClick,
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(56.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                ) {
                    Text(text = stringResource(R.string.record_route))
                }
            }
        }

//        // Navigation Bar
//        BottomNavigationBar(
//            modifier =
//                Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter),
//        )
    }
}
