package ar.edu.unlam.scaffoldingandroid3.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room - Tabla de actividades completadas (historial)
 * @Entity, @PrimaryKey, propiedades: id, routeName, completedAt, totalDistance, duration, photos
 * Para mostrar en pantalla "Tu historial de actividad"
 */

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routeName: String,
    val date: String,
    val metricsJson: String,
    val photosJson: String,
    val routePointsJson: String,
)
