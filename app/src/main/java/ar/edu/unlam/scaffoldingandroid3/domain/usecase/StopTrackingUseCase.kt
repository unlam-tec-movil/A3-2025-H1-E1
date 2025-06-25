package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.SensorRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.TrackingSessionRepository
import javax.inject.Inject

/**
 * Caso de uso - Finalizar sesión de tracking
 * Detiene GPS, calcula métricas finales y guarda ruta completada
 * Compatible con CU-014: Finalizar grabación de ruta
 *
 * Respeta Clean Architecture: Use Case → Repository → Service
 */
class StopTrackingUseCase
    @Inject
    constructor(
        private val trackingSessionRepository: TrackingSessionRepository,
        private val locationRepository: LocationRepository,
        private val sensorRepository: SensorRepository,
    ) {
        /**
         * Finaliza la sesión de tracking actual
         * @return Result con la sesión finalizada o error
         */
        suspend fun execute(): Result<TrackingSession> {
            return try {
                // 1. Verificar que hay una sesión activa
                val currentSession =
                    trackingSessionRepository.getCurrentTrackingSession()
                        ?: return Result.failure(Exception("No hay sesión de tracking activa"))

                // 2. Detener tracking de ubicación
                locationRepository.stopLocationTracking()

                // 3. Detener tracking de sensores
                sensorRepository.stopSensorTracking()

                // 4. Finalizar sesión y obtener datos finales
                val finalSession =
                    trackingSessionRepository.stopTrackingSession()
                        ?: return Result.failure(Exception("Error al finalizar sesión"))

                // 5. Permitir guardado incluso sin puntos GPS (útil para simuladores y testing)
                // El usuario puede decidir si vale la pena guardarlo en SaveRoute

                // 6. Sesión finalizada - se guardará solo si usuario confirma en SaveRoute
                // NO guardar automáticamente aquí para evitar duplicados

                Result.success(finalSession)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        /**
         * Verifica si hay una sesión que se puede finalizar
         */
        suspend fun canStopTracking(): Boolean {
            return trackingSessionRepository.hasActiveSession()
        }

        /**
         * Obtiene un resumen de la sesión antes de finalizar
         */
        suspend fun getSessionSummary(): Map<String, Any>? {
            val session = trackingSessionRepository.getCurrentTrackingSession() ?: return null
            val elapsedTime = trackingSessionRepository.getElapsedTime()

            return mapOf(
                "routeName" to session.routeName,
                "elapsedTime" to elapsedTime,
                "pointsCount" to session.routePoint.size,
                "distance" to session.metrics.currentDistance,
                "averageSpeed" to session.metrics.averageSpeed,
                "maxSpeed" to session.metrics.maxSpeed,
                "elevationGain" to session.metrics.totalElevationGain,
                "elevationLoss" to session.metrics.totalElevationLoss,
            )
        }
    }
