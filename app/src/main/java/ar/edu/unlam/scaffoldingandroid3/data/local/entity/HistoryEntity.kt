package ar.edu.unlam.scaffoldingandroid3.data.local.entity

/**
 * TODO: Entity Room - Tabla de actividades completadas (historial)
 * @Entity, @PrimaryKey, propiedades: id, routeName, completedAt, totalDistance, duration, photos
 * Para mostrar en pantalla "Tu historial de actividad"
 */

data class HistoryEntity(val id: Long = 0)
