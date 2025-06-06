package ar.edu.unlam.scaffoldingandroid3.data.local.entity

/**
 * TODO: Entity Room - Tabla de puntos GPS durante tracking
 * @Entity, @PrimaryKey, @ForeignKey a TrackingSessionEntity
 * Propiedades: id, sessionId, latitude, longitude, timestamp, accuracy, speed, altitude
 */

data class LocationPointEntity(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)
