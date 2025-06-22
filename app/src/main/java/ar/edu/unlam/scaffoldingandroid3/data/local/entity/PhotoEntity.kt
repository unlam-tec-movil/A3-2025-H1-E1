package ar.edu.unlam.scaffoldingandroid3.data.local.entity

import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * TODO: Entity Room - Tabla de fotos tomadas durante recorridos
 * @Entity, @PrimaryKey, @ForeignKey a RouteEntity
 * Propiedades: id, routeId, filePath, name, description, latitude, longitude, timestamp, fileSize
 */

data class PhotoEntity(
    @PrimaryKey val id: Long = 0,
    val RouteEntity: String,
    val filePath: String? = null
)
