package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.History
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingResult
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingStatus
import ar.edu.unlam.scaffoldingandroid3.domain.repository.HistoryRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.RouteRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.TrackingSessionRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * Caso de uso que persiste la información de un recorrido completado.
 *
 * 1. Guarda la TrackingSession (detalle técnico completo)
 * 2. Guarda la Route para la pantalla "Mis rutas"
 * 3. Guarda el History para la pantalla "Historial"
 */
class SaveTrackingResultUseCase
    @Inject
    constructor(
        private val trackingSessionRepository: TrackingSessionRepository,
        private val routeRepository: RouteRepository,
        private val historyRepository: HistoryRepository,
    ) {
        /**
         * Persiste la información y devuelve el ID de la TrackingSession guardada.
         */
        suspend fun execute(trackingResult: TrackingResult): Result<Long> {
            return try {
                // 1) Crear y guardar TrackingSession
                val session = buildTrackingSession(trackingResult)
                val sessionId = trackingSessionRepository.saveTrackingSession(session)

                // 2) Crear y guardar Route
                val route = buildRoute(trackingResult)
                routeRepository.saveRoute(route)

                // 3) Crear y guardar History
                val history = buildHistory(trackingResult)
                historyRepository.saveCompletedActivity(history)

                Result.success(sessionId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        private fun buildTrackingSession(result: TrackingResult): TrackingSession {
            return TrackingSession(
                id = 0L,
                routeName = result.nombreRecorrido,
                status = TrackingStatus.COMPLETED,
                startTime = result.fechaCreacion,
                endTime = System.currentTimeMillis(),
                routePoint = result.rutaCompleta,
                metrics =
                    TrackingMetrics(
                        currentSpeed = 0.0,
                        averageSpeed = result.velocidadMedia,
                        maxSpeed = result.velocidadMaxima,
                        currentDistance = result.distanciaTotal,
                        currentDuration = parseDurationStringToMillis(result.duracion),
                        currentElevation = result.altitudMaxima,
                        minElevation = result.altitudMinima,
                        maxElevation = result.altitudMaxima,
                        totalSteps = result.pasosTotales,
                        lastLocation = result.rutaCompleta.lastOrNull(),
                    ),
            )
        }

        private fun buildRoute(result: TrackingResult): Route {
            val routePoints =
                result.rutaCompleta.map { lp: LocationPoint ->
                    Route.Point(
                        latitude = lp.latitude,
                        longitude = lp.longitude,
                        timestamp = lp.timestamp,
                    )
                }

            return Route(
                id = UUID.randomUUID().toString(),
                name = result.nombreRecorrido,
                points = routePoints,
                distance = result.distanciaTotal,
                duration = parseDurationStringToMillis(result.duracion),
                photoUri = result.fotosCapturadas.firstOrNull()?.uri ?: "",
            )
        }

        private fun buildHistory(result: TrackingResult): History {
            val metrics =
                TrackingMetrics(
                    averageSpeed = result.velocidadMedia,
                    maxSpeed = result.velocidadMaxima,
                    currentDistance = result.distanciaTotal,
                    currentDuration = parseDurationStringToMillis(result.duracion),
                    minElevation = result.altitudMinima,
                    maxElevation = result.altitudMaxima,
                    totalSteps = result.pasosTotales,
                )

            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(result.fechaCreacion))

            return History(
                routeName = result.nombreRecorrido,
                date = dateStr,
                metrics = metrics,
                photoUri = result.fotosCapturadas.firstOrNull()?.uri ?: "",
                routePoint = result.rutaCompleta,
            )
        }

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
