package ar.edu.unlam.scaffoldingandroid3.domain.repository

import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingStatus
import kotlinx.coroutines.flow.Flow

/**
 * Puerto de salida - Gestión de sesiones de tracking
 * Define métodos para CRUD de sesiones de tracking GPS con estado y métricas
 * Compatible con CU-010 a CU-014: manejo completo del estado de tracking
 */
interface TrackingSessionRepository {
    
    /**
     * Inicia una nueva sesión de tracking
     */
    suspend fun startTrackingSession(routeName: String): TrackingSession
    
    /**
     * Pausa la sesión de tracking actual
     */
    suspend fun pauseTrackingSession(): TrackingSession?
    
    /**
     * Reanuda la sesión de tracking pausada
     */
    suspend fun resumeTrackingSession(): TrackingSession?
    
    /**
     * Finaliza la sesión de tracking actual
     */
    suspend fun stopTrackingSession(): TrackingSession?
    
    /**
     * Obtiene la sesión de tracking actual
     */
    suspend fun getCurrentTrackingSession(): TrackingSession?
    
    /**
     * Obtiene el estado actual del tracking
     */
    fun getTrackingStatus(): Flow<TrackingStatus>
    
    /**
     * Obtiene las métricas en tiempo real
     */
    fun getCurrentMetrics(): Flow<TrackingMetrics>
    
    /**
     * Guarda una sesión de tracking completa
     */
    suspend fun saveTrackingSession(session: TrackingSession): Long
    
    /**
     * Verifica si hay una sesión activa
     */
    suspend fun hasActiveSession(): Boolean
    
    /**
     * Obtiene el tiempo transcurrido de la sesión actual
     */
    suspend fun getElapsedTime(): Long
}
