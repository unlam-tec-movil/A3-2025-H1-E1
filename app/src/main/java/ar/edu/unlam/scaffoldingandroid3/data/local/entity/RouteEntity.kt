package ar.edu.unlam.scaffoldingandroid3.data.local.entity

/**
 * TODO: Entity Room - Tabla de rutas guardadas/favoritas
 * @Entity, @PrimaryKey, propiedades: id, name, distance, duration, createdAt, isFavorite
 * Relación 1:N con PhotoEntity y LocationPointEntity
 */

data class RouteEntity(val id: Long = 0)
