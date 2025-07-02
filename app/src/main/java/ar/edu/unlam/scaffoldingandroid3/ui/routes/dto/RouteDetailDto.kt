package ar.edu.unlam.scaffoldingandroid3.ui.routes.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint

/**
 * DTO para navegación - Métricas de tracking serializables
 * Mantiene el dominio libre de dependencias de Android
 */
@Parcelize
data class TrackingMetricsDto(
    val id: Long = 0,
    val currentSpeed: Double = 0.0,
    val averageSpeed: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val currentDistance: Double = 0.0,
    val currentDuration: Long = 0,
    val currentElevation: Double = 0.0,
    val minElevation: Double = 0.0,
    val maxElevation: Double = 0.0,
    val totalSteps: Int = 0,
    val lastLocation: LocationPointDto? = null,
) : Parcelable

/**
 * DTO para navegación - Punto de ubicación serializable
 * Mantiene el dominio libre de dependencias de Android
 */
@Parcelize
data class LocationPointDto(
    val accuracy: Float,
    val speed: Float?,
    val altitude: Double?,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
) : Parcelable

/**
 * Mappers entre domain y DTO
 */
fun TrackingMetrics.toDto(): TrackingMetricsDto {
    return TrackingMetricsDto(
        id = id,
        currentSpeed = currentSpeed,
        averageSpeed = averageSpeed,
        maxSpeed = maxSpeed,
        currentDistance = currentDistance,
        currentDuration = currentDuration,
        currentElevation = currentElevation,
        minElevation = minElevation,
        maxElevation = maxElevation,
        totalSteps = totalSteps,
        lastLocation = lastLocation?.toDto()
    )
}

fun TrackingMetricsDto.toDomain(): TrackingMetrics {
    return TrackingMetrics(
        id = id,
        currentSpeed = currentSpeed,
        averageSpeed = averageSpeed,
        maxSpeed = maxSpeed,
        currentDistance = currentDistance,
        currentDuration = currentDuration,
        currentElevation = currentElevation,
        minElevation = minElevation,
        maxElevation = maxElevation,
        totalSteps = totalSteps,
        lastLocation = lastLocation?.toDomain()
    )
}

fun LocationPoint.toDto(): LocationPointDto {
    return LocationPointDto(
        accuracy = accuracy,
        speed = speed,
        altitude = altitude,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp
    )
}

fun LocationPointDto.toDomain(): LocationPoint {
    return LocationPoint(
        accuracy = accuracy,
        speed = speed,
        altitude = altitude,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp
    )
} 