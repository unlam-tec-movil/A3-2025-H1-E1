package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingResult
import ar.edu.unlam.scaffoldingandroid3.domain.repository.TrackingSessionRepository
import javax.inject.Inject

/**
 * Use Case para guardar resultados de tracking en base de datos
 * Compatible con Clean Architecture: ViewModel → UseCase → Repository
 * Requerimiento TRACKING_REQUIREMENTS.md líneas 254-259
 */
class SaveTrackingResultUseCase @Inject constructor(
    private val trackingSessionRepository: TrackingSessionRepository
) {
    
    /**
     * Guarda el resultado completo de tracking en base de datos
     */
    suspend fun execute(trackingResult: TrackingResult): Result<Long> {
        return try {
            // Crear sesión de dominio a partir del resultado
            val session = ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession(
                id = 0L, // Se asignará automáticamente
                routeName = trackingResult.nombreRecorrido,
                status = ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingStatus.COMPLETED,
                startTime = trackingResult.fechaCreacion,
                endTime = System.currentTimeMillis(),
                routePoint = trackingResult.rutaCompleta,
                metrics = ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics(
                    currentDistance = trackingResult.distanciaTotal,
                    averageSpeed = trackingResult.velocidadMedia,
                    maxSpeed = trackingResult.velocidadMaxima,
                    currentElevation = trackingResult.altitudMaxima, // Usar la altitud máxima
                    totalElevationGain = trackingResult.altitudMaxima - trackingResult.altitudMinima,
                    currentSpeed = 0.0, // No relevante para sesión completada
                    currentDuration = 0L, // Se calculará en el repository
                    totalElevationLoss = 0.0, // Simplificado por ahora
                    lastLocation = null // No disponible en resultado final
                )
            )
            
            // Guardar a través del repository (respeta Clean Architecture)
            val sessionId = trackingSessionRepository.saveTrackingSession(session)
            
            Result.success(sessionId)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}