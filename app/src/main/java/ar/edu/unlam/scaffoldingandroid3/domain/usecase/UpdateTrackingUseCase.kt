package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingStatus
import ar.edu.unlam.scaffoldingandroid3.domain.repository.TrackingSessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso - Actualizar tracking con métricas en tiempo real
 * Gestiona pausar/reanudar y proporciona métricas actualizadas
 * Compatible con CU-011, CU-012, CU-013: pausar, reanudar, ver estadísticas
 *
 * Respeta Clean Architecture: Use Case → Repository → Service
 */
class UpdateTrackingUseCase
    @Inject
    constructor(
        private val trackingSessionRepository: TrackingSessionRepository,
    ) {
        /**
         * Pausa la sesión de tracking actual
         * Compatible con CU-011: Pausar grabación de ruta
         */
        suspend fun pauseTracking(): Result<Unit> {
            return try {
                val session = trackingSessionRepository.pauseTrackingSession()
                if (session != null) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("No hay sesión activa para pausar"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        /**
         * Reanuda la sesión de tracking pausada
         * Compatible con CU-012: Reanudar grabación pausada
         */
        suspend fun resumeTracking(): Result<Unit> {
            return try {
                val session = trackingSessionRepository.resumeTrackingSession()
                if (session != null) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("No hay sesión pausada para reanudar"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        /**
         * Obtiene las métricas en tiempo real
         * Compatible con CU-013: Ver estadísticas durante grabación
         */
        fun getCurrentMetrics(): Flow<TrackingMetrics> {
            return trackingSessionRepository.getCurrentMetrics()
        }

        /**
         * Obtiene el estado actual del tracking
         */
        fun getTrackingStatus(): Flow<TrackingStatus> {
            return trackingSessionRepository.getTrackingStatus()
        }

        /**
         * Obtiene el tiempo EN MOVIMIENTO (sin pausas)
         */
        suspend fun getMovementTime(): Long {
            return trackingSessionRepository.getElapsedTime() // Mantener compatibilidad
        }
        
        /**
         * Obtiene el tiempo TOTAL (con pausas)
         */
        suspend fun getTotalTime(): Long {
            // Por ahora usar el mismo valor, será mejorado cuando TrackingSessionRepository se actualice
            return trackingSessionRepository.getElapsedTime()
        }
        
        /**
         * DEPRECATED - Usar getMovementTime()
         */
        @Deprecated("Use getMovementTime()")
        suspend fun getElapsedTime(): Long {
            return trackingSessionRepository.getElapsedTime()
        }

        /**
         * Verifica si se puede pausar el tracking
         */
        suspend fun canPauseTracking(): Boolean {
            val session = trackingSessionRepository.getCurrentTrackingSession()
            return session?.status == TrackingStatus.ACTIVE
        }

        /**
         * Verifica si se puede reanudar el tracking
         */
        suspend fun canResumeTracking(): Boolean {
            val session = trackingSessionRepository.getCurrentTrackingSession()
            return session?.status == TrackingStatus.PAUSED
        }

        /**
         * Obtiene estadísticas completas actuales
         * Compatible con CU-013: estadísticas detalladas
         */
        suspend fun getDetailedStats(): Map<String, Any>? {
            val session = trackingSessionRepository.getCurrentTrackingSession() ?: return null
            
            // Usar el tiempo desde currentDuration que se actualiza cada segundo
            val currentTime = session.metrics.currentDuration
            val movementTime = currentTime // Por ahora son iguales, pero separados conceptualmente
            val totalTime = currentTime // Por ahora son iguales
            
            // Obtener datos adicionales de métricas del session (respeta Clean Architecture)
            val metrics = session.metrics

            return mapOf(
                "routeName" to session.routeName,
                "status" to session.status.name,
                // Tiempos separados según especificación
                "movementTime" to movementTime,
                "movementTimeFormatted" to formatTime(movementTime),
                "totalTime" to totalTime,
                "totalTimeFormatted" to formatTime(totalTime),
                // Para compatibilidad (mostrar tiempo de movimiento por defecto)
                "elapsedTime" to movementTime,
                "elapsedTimeFormatted" to formatTime(movementTime),
                // Velocidades y distancias
                "currentSpeed" to session.metrics.currentSpeed,
                "averageSpeed" to session.metrics.averageSpeed, // Ahora calculado correctamente
                "maxSpeed" to session.metrics.maxSpeed,
                "distance" to session.metrics.currentDistance,
                "distanceFormatted" to "%.2f km".format(session.metrics.currentDistance),
                // Altitudes con min/max
                "currentElevation" to session.metrics.currentElevation,
                "elevationGain" to session.metrics.totalElevationGain,
                "elevationLoss" to session.metrics.totalElevationLoss,
                "minAltitude" to session.metrics.currentElevation, // Simplificado por ahora
                "maxAltitude" to session.metrics.currentElevation, // Simplificado por ahora
                "pointsCount" to session.routePoint.size,
                "lastLocation" to (session.metrics.lastLocation ?: "No disponible"),
                // Datos de sensores ya disponibles en metrics
                "totalSteps" to 0, // Simplificado - se obtiene del UI state
                "totalMovementTime" to movementTime,
            )
        }

        /**
         * Formatea tiempo en milisegundos a HH:MM:SS
         */
        private fun formatTime(milliseconds: Long): String {
            val seconds = milliseconds / 1000
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60

            return String.format("%02d:%02d:%02d", hours, minutes, secs)
        }
    }
