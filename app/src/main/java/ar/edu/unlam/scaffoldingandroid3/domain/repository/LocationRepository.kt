package ar.edu.unlam.scaffoldingandroid3.domain.repository

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

/**
 * Puerto de salida - Servicios de ubicación GPS
 * Define métodos para tracking GPS, permisos y actualizaciones de ubicación
 * Compatible con CU-010 a CU-014 del documento de casos de uso
 */
interface LocationRepository {
    
    /**
     * Obtiene actualizaciones de ubicación en tiempo real
     */
    fun getLocationUpdates(): Flow<LocationPoint>
    
    /**
     * Obtiene la última ubicación conocida
     */
    suspend fun getLastKnownLocation(): LocationPoint?
    
    /**
     * Inicia el tracking de ubicación
     */
    suspend fun startLocationTracking(): Boolean
    
    /**
     * Detiene el tracking de ubicación
     */
    suspend fun stopLocationTracking()
    
    /**
     * Pausa el tracking de ubicación (mantiene el servicio)
     */
    suspend fun pauseLocationTracking()
    
    /**
     * Reanuda el tracking de ubicación
     */
    suspend fun resumeLocationTracking()
    
    /**
     * Verifica si se tienen los permisos necesarios
     */
    fun hasLocationPermissions(): Boolean
    
    /**
     * Verifica si se tiene permiso de ubicación en background
     */
    fun hasBackgroundLocationPermission(): Boolean
    
    /**
     * Verifica si está actualmente trackeando
     */
    fun isTracking(): Boolean
}
