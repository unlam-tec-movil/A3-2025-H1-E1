package ar.edu.unlam.scaffoldingandroid3.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity Room - Puntos GPS de tracking
 * Almacena la ruta completa asociada a una sesión de tracking
 */
@Entity(
    tableName = "location_points",
    foreignKeys = [
        ForeignKey(
            entity = TrackingSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class LocationPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val accuracy: Float?,
    val speed: Float?,
    val timestamp: Long
)
