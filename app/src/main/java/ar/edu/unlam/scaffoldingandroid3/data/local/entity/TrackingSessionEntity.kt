package ar.edu.unlam.scaffoldingandroid3.data.local.entity

/**
 * TODO: Entity Room - Tabla de sesiones de tracking activas
 * @Entity, @PrimaryKey, propiedades: id, startTime, endTime, status
 * Relación 1:N con LocationPointEntity para puntos GPS del tracking
 */

data class TrackingSessionEntity(val id: Long = 0)
