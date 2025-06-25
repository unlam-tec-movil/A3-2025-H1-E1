package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingResult
import ar.edu.unlam.scaffoldingandroid3.domain.repository.TrackingSessionRepository
import javax.inject.Inject

/**
 * Use Case para guardar resultados de tracking en base de datos
 * Compatible con Clean Architecture: ViewModel → UseCase → Repository
 */
class SaveTrackingResultUseCase
    @Inject
    constructor(
        private val trackingSessionRepository: TrackingSessionRepository,
    ) {
        /**
         * Guarda el resultado completo de tracking en base de datos
         */
        suspend fun execute(trackingResult: TrackingResult): Result<Long> {
            return try {
                // Crear sesión de dominio a partir del resultado
                val session =
                    ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession(
                        // Se asignará automáticamente
                        id = 0L,
                        routeName = trackingResult.nombreRecorrido,
                        status = ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingStatus.COMPLETED,
                        startTime = trackingResult.fechaCreacion,
                        endTime = System.currentTimeMillis(),
                        routePoint = trackingResult.rutaCompleta,
                        metrics =
                            ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics(
                                currentDistance = trackingResult.distanciaTotal,
                                averageSpeed = trackingResult.velocidadMedia,
                                maxSpeed = trackingResult.velocidadMaxima,
                                // Usar la altitud máxima
                                currentElevation = trackingResult.altitudMaxima,
                                totalElevationGain = trackingResult.altitudMaxima - trackingResult.altitudMinima,
                                // No relevante para sesión completada
                                currentSpeed = 0.0,
                                // Se calculará en el repository
                                currentDuration = 0L,
                                // Simplificado por ahora
                                totalElevationLoss = 0.0,
                                // No disponible en resultado final
                                lastLocation = null,
                            ),
                    )

                // Guardar a través del repository (respeta Clean Architecture)
                val sessionId = trackingSessionRepository.saveTrackingSession(session)

                Result.success(sessionId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
