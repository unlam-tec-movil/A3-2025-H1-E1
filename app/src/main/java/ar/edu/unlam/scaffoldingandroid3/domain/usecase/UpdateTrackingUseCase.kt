package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingStatus
import ar.edu.unlam.scaffoldingandroid3.domain.repository.TrackingSessionRepository
import javax.inject.Inject

/**
 * Caso de uso - Operaciones de tracking con LÓGICA DE NEGOCIO
 * Solo contiene pausar/reanudar que requieren validación de estado
 * Compatible con CU-011, CU-012: pausar, reanudar
 *
 * NOTA: Operaciones simples como getCurrentMetrics(), getTrackingStatus()
 * se acceden directamente desde ViewModel → Repository (no pass-through)
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

        // ELIMINADOS: getCurrentMetrics(), getTrackingStatus(), getMovementTime(), etc.
        // Son pass-through sin lógica de negocio
        // ViewModel debe acceder directamente al Repository para estas operaciones

        /**
         * Verifica si se puede pausar el tracking - LÓGICA DE NEGOCIO
         * Validación de estado necesaria antes de pausar
         */
        suspend fun canPauseTracking(): Boolean {
            val session = trackingSessionRepository.getCurrentTrackingSession()
            return session?.status == TrackingStatus.ACTIVE
        }

        /**
         * Verifica si se puede reanudar el tracking - LÓGICA DE NEGOCIO  
         * Validación de estado necesaria antes de reanudar
         */
        suspend fun canResumeTracking(): Boolean {
            val session = trackingSessionRepository.getCurrentTrackingSession()
            return session?.status == TrackingStatus.PAUSED
        }

        // ELIMINADO: getDetailedStats() - es pass-through complejo
        // ViewModel puede acceder directamente a Repository.getDetailedStats()
        // o Repository.getCurrentTrackingSession() y procesar los datos directamente
    }
