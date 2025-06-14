package ar.edu.unlam.scaffoldingandroid3.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route

/**
 * @Entity, @PrimaryKey, propiedades: id, name, distance, duration, createdAt, isFavorite
 * Relación 1:N con PhotoEntity y LocationPointEntity
 */

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val id: String,
    val name: String,
    val points: List<Route.Point>,
    val distance: Double,
    val duration: Long,
)
