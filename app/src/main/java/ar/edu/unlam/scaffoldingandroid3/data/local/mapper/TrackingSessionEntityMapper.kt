package ar.edu.unlam.scaffoldingandroid3.data.local.mapper

import ar.edu.unlam.scaffoldingandroid3.data.local.entity.TrackingSessionEntity
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingStatus

/**
 * Mapper para conversión TrackingSession (domain) ↔ TrackingSessionEntity (data)
 * Implementación sencilla y funcional para Clean Architecture
 */
object TrackingSessionEntityMapper {

    /**
     * Convierte TrackingSession (domain) a TrackingSessionEntity (data)
     */
    fun TrackingSession.toEntity(): TrackingSessionEntity {
        return TrackingSessionEntity(
            id = this.id,
            routeName = this.routeName,
            startTime = this.startTime,
            endTime = this.endTime,
            totalDuration = formatDuration(this.endTime - this.startTime),
            movingDuration = formatDuration(this.metrics.currentDuration),
            totalDistance = this.metrics.currentDistance,
            totalSteps = 0, // Se actualiza desde sensores si es necesario
            averageSpeed = this.metrics.averageSpeed,
            maxSpeed = this.metrics.maxSpeed,
            minAltitude = this.metrics.currentElevation,
            maxAltitude = this.metrics.currentElevation,
            createdAt = System.currentTimeMillis()
        )
    }

    /**
     * Convierte TrackingSessionEntity (data) a TrackingSession (domain)
     */
    fun TrackingSessionEntity.toDomain(): TrackingSession {
        return TrackingSession(
            id = this.id,
            routeName = this.routeName,
            startTime = this.startTime,
            endTime = this.endTime,
            metrics = TrackingMetrics(
                currentDistance = this.totalDistance,
                averageSpeed = this.averageSpeed,
                maxSpeed = this.maxSpeed,
                currentSpeed = 0.0, // No se persiste velocidad actual
                currentElevation = this.maxAltitude,
                currentDuration = parseDuration(this.movingDuration)
            ),
            status = TrackingStatus.COMPLETED, // Sessions guardadas están completas
            routePoint = emptyList() // Los puntos se manejan por separado
        )
    }

    /**
     * Formatea duración en milisegundos a formato HH:MM:SS
     */
    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = millis / (1000 * 60 * 60)
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    /**
     * Parsea formato HH:MM:SS a milisegundos
     */
    private fun parseDuration(duration: String): Long {
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
