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
                                // Usar la altitud máxima como actual
                                currentElevation = trackingResult.altitudMaxima,
                                // Mapear altitudes min/max correctamente
                                minElevation = trackingResult.altitudMinima,
                                maxElevation = trackingResult.altitudMaxima,
                                // No relevante para sesión completada
                                currentSpeed = 0.0,
                                // Convertir duración de string a milisegundos
                                currentDuration = parseDurationStringToMillis(trackingResult.duracion),
                                // No disponible en resultado final
                                lastLocation = null,
                                // Mapear pasos totales correctamente
                                totalSteps = trackingResult.pasosTotales,
                            ),
                    )

                // Guardar a través del repository (respeta Clean Architecture)
                val sessionId = trackingSessionRepository.saveTrackingSession(session)

                Result.success(sessionId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        /**
         * Convierte string de duración HH:MM:SS a milisegundos
         */
        private fun parseDurationStringToMillis(duration: String): Long {
            return try {
                val parts = duration.split(":")
                if (parts.size == 3) {
                    val hours = parts[0].toLong()
                    val minutes = parts[1].toLong()
                    val seconds = parts[2].toLong()
                    (hours * 3600 + minutes * 60 + seconds) * 1000
                } else {
                    0L
                }
            } catch (e: Exception) {
                0L
            }
        }
    }
