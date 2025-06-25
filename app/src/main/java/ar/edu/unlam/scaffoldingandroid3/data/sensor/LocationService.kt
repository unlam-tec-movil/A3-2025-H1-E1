package ar.edu.unlam.scaffoldingandroid3.data.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio de localización GPS para tracking en tiempo real
 * Compatible con CU-010 a CU-014: tracking continuo, pausar/reanudar
 */
@Singleton
class LocationService
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        private val locationRequest =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_UPDATE_INTERVAL,
            ).apply {
                setMinUpdateDistanceMeters(MIN_DISTANCE_CHANGE_FOR_UPDATES)
                setMinUpdateIntervalMillis(FASTEST_LOCATION_UPDATE_INTERVAL)
                setMaxUpdateDelayMillis(MAX_UPDATE_DELAY)
                setWaitForAccurateLocation(false) // Más fluido, menos espera
                setGranularity(com.google.android.gms.location.Granularity.GRANULARITY_PERMISSION_LEVEL)
            }.build()

        private var isTracking = false
        private var locationCallback: LocationCallback? = null

        /**
         * Obtiene actualizaciones de ubicación en tiempo real
         * @return Flow de Location para tracking continuo
         */
        fun getLocationUpdates(): Flow<Location> =
            callbackFlow {
                if (!hasLocationPermission()) {
                    close(SecurityException("Permisos de ubicación no otorgados"))
                    return@callbackFlow
                }

                locationCallback =
                    object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            result.lastLocation?.let { location ->
                                // Aceptar casi todas las ubicaciones para mejor fluidez
                                if (location.accuracy <= MAX_ACCURACY_THRESHOLD) {
                                    trySend(location)
                                } else {
                                    trySend(location) // Enviar de todas maneras para fluidez
                                }
                            }
                        }

                        override fun onLocationAvailability(availability: LocationAvailability) {
                            // GPS availability status tracking
                        }
                    }

                try {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback!!,
                        Looper.getMainLooper(),
                    )
                    isTracking = true
                } catch (e: SecurityException) {
                    close(e)
                    return@callbackFlow
                }

                awaitClose {
                    stopLocationUpdates()
                }
            } // Remover distinctUntilChanged para máxima fluidez
        // Google Maps no filtra tanto - necesitamos todas las actualizaciones

        /**
         * Obtiene la última ubicación conocida
         */
        suspend fun getLastKnownLocation(): Location? {
            if (!hasLocationPermission()) return null

            return try {
                @Suppress("MissingPermission")
                fusedLocationClient.lastLocation.await()
            } catch (e: SecurityException) {
                // Permisos denegados después de verificación
                null
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Detiene las actualizaciones de ubicación (para pausar tracking)
         */
        fun stopLocationUpdates() {
            locationCallback?.let { callback ->
                fusedLocationClient.removeLocationUpdates(callback)
                isTracking = false
            }
        }

        /**
         * Reanuda las actualizaciones de ubicación
         */
        fun resumeLocationUpdates() {
            if (!isTracking && hasLocationPermission()) {
                locationCallback?.let { callback ->
                    try {
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            callback,
                            Looper.getMainLooper(),
                        )
                        isTracking = true
                    } catch (e: SecurityException) {
                        // Handle permission error
                    }
                }
            }
        }

        /**
         * Calcula la distancia entre dos ubicaciones en metros
         */
        fun calculateDistance(
            startLocation: Location,
            endLocation: Location,
        ): Float {
            return startLocation.distanceTo(endLocation)
        }

        /**
         * Calcula la velocidad actual en km/h
         */
        fun calculateSpeed(location: Location): Double {
            return if (location.hasSpeed()) {
                (location.speed * 3.6) // m/s to km/h
            } else {
                0.0
            }
        }

        /**
         * Obtiene la altitud actual en metros
         */
        fun getAltitude(location: Location): Double {
            return if (location.hasAltitude()) {
                location.altitude
            } else {
                0.0
            }
        }

        /**
         * Verifica la precisión de la ubicación
         */
        fun isLocationAccurate(location: Location): Boolean {
            return location.accuracy <= MAX_ACCURACY_THRESHOLD
        }

        /**
         * Verifica si se tienen los permisos de ubicación necesarios
         */
        fun hasLocationPermission(): Boolean {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
        }

        /**
         * Verifica si se tiene permiso de ubicación en background
         */
        fun hasBackgroundLocationPermission(): Boolean {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun isTracking(): Boolean = isTracking

        companion object {
            // Configuración AGRESIVA para tracking SÚPER fluido como Google Maps
            private const val LOCATION_UPDATE_INTERVAL = 500L // 500ms (más fluido que antes)
            private const val FASTEST_LOCATION_UPDATE_INTERVAL = 250L // 250ms (muy responsivo)
            private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 1.0f // 1 metro (balance detalle/ruido)
            private const val MAX_UPDATE_DELAY = 1000L // 1 segundo (muy rápido)
            private const val MAX_ACCURACY_THRESHOLD = 30.0f // 30 metros (más permisivo para interiores)
        }
    }
