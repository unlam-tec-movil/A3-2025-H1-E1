package ar.edu.unlam.scaffoldingandroid3.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room - Tabla de sesiones de tracking completadas
 */
@Entity(tableName = "tracking_sessions")
data class TrackingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routeName: String,
    val startTime: Long,
    val endTime: Long,
    val totalDuration: String,
    val movingDuration: String,
    val totalDistance: Double,
    val totalSteps: Int,
    val averageSpeed: Double,
    val maxSpeed: Double,
    val minAltitude: Double,
    val maxAltitude: Double,
    val createdAt: Long,
    val photo: String,
)
