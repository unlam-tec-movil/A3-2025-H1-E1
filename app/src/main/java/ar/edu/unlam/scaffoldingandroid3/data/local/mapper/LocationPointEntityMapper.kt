package ar.edu.unlam.scaffoldingandroid3.data.local.mapper

import ar.edu.unlam.scaffoldingandroid3.data.local.entity.LocationPointEntity
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint

/**
 * Mapper - Conversión entre Domain y Data Layer
 */
object LocationPointEntityMapper {

    /**
     * Convierte LocationPoint (domain) a LocationPointEntity (data)
     */
    fun LocationPoint.toEntity(sessionId: Long): LocationPointEntity {
        return LocationPointEntity(
            sessionId = sessionId,
            latitude = this.latitude,
            longitude = this.longitude,
            altitude = this.altitude,
            accuracy = this.accuracy,
            speed = this.speed,
            timestamp = this.timestamp
        )
    }

    /**
     * Convierte LocationPointEntity (data) a LocationPoint (domain)
     */
    fun LocationPointEntity.toDomain(): LocationPoint {
        return LocationPoint(
            latitude = this.latitude,
            longitude = this.longitude,
            altitude = this.altitude,
            accuracy = this.accuracy ?: 0.0f, // Default si es null
            speed = this.speed,
            timestamp = this.timestamp
        )
    }

    /**
     * Convierte lista de LocationPoint a lista de LocationPointEntity
     */
    fun List<LocationPoint>.toEntityList(sessionId: Long): List<LocationPointEntity> {
        return this.map { it.toEntity(sessionId) }
    }

    /**
     * Convierte lista de LocationPointEntity a lista de LocationPoint
     */
    fun List<LocationPointEntity>.toDomainList(): List<LocationPoint> {
        return this.map { it.toDomain() }
    }
}
