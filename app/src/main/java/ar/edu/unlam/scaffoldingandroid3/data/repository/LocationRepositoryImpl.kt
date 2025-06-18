package ar.edu.unlam.scaffoldingandroid3.data.repository

import ar.edu.unlam.scaffoldingandroid3.data.sensor.LocationService
import ar.edu.unlam.scaffoldingandroid3.data.permissions.PermissionsHandler
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del LocationRepository - Adaptador para servicios GPS
 * Conecta el dominio con los servicios de Android (LocationService)
 * Respeta Clean Architecture: Domain → Data → Android Services
 */
@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationService: LocationService,
    private val permissionsHandler: PermissionsHandler
) : LocationRepository {

    /**
     * Obtiene actualizaciones de ubicación en tiempo real
     * Convierte Location de Android a LocationPoint del dominio
     */
    override fun getLocationUpdates(): Flow<LocationPoint> {
        return locationService.getLocationUpdates()
            .map { androidLocation ->
                LocationPoint(
                    latitude = androidLocation.latitude,
                    longitude = androidLocation.longitude,
                    altitude = if (androidLocation.hasAltitude()) androidLocation.altitude else null,
                    accuracy = androidLocation.accuracy,
                    speed = if (androidLocation.hasSpeed()) androidLocation.speed else null,
                    timestamp = androidLocation.time
                )
            }
    }

    /**
     * Obtiene la última ubicación conocida
     */
    override suspend fun getLastKnownLocation(): LocationPoint? {
        return locationService.getLastKnownLocation()?.let { androidLocation ->
            LocationPoint(
                latitude = androidLocation.latitude,
                longitude = androidLocation.longitude,
                altitude = if (androidLocation.hasAltitude()) androidLocation.altitude else null,
                accuracy = androidLocation.accuracy,
                speed = if (androidLocation.hasSpeed()) androidLocation.speed else null,
                timestamp = androidLocation.time
            )
        }
    }

    /**
     * Inicia el tracking de ubicación
     */
    override suspend fun startLocationTracking(): Boolean {
        return if (hasLocationPermissions()) {
            // El LocationService maneja el tracking a través de flows
            true
        } else {
            false
        }
    }

    /**
     * Detiene el tracking de ubicación
     */
    override suspend fun stopLocationTracking() {
        locationService.stopLocationUpdates()
    }

    /**
     * Pausa el tracking de ubicación
     */
    override suspend fun pauseLocationTracking() {
        locationService.stopLocationUpdates()
    }

    /**
     * Reanuda el tracking de ubicación
     */
    override suspend fun resumeLocationTracking() {
        locationService.resumeLocationUpdates()
    }

    /**
     * Verifica si se tienen los permisos necesarios
     */
    override fun hasLocationPermissions(): Boolean {
        return permissionsHandler.hasLocationPermissions()
    }

    /**
     * Verifica si se tiene permiso de ubicación en background
     */
    override fun hasBackgroundLocationPermission(): Boolean {
        return permissionsHandler.hasBackgroundLocationPermission()
    }

    /**
     * Verifica si está actualmente trackeando
     */
    override fun isTracking(): Boolean {
        return locationService.isTracking()
    }
}
