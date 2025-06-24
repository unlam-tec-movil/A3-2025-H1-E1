package ar.edu.unlam.scaffoldingandroid3.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity Room - Tabla de fotos tomadas durante tracking (simplificado)
 * Compatible con Clean Architecture - las fotos pertenecen a la ruta
 */
@Entity(
    tableName = "tracking_photos",
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
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val uri: String,
    val orderInRoute: Int,
)
