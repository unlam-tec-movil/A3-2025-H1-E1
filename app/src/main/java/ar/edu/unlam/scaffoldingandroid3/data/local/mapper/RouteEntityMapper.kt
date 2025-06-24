package ar.edu.unlam.scaffoldingandroid3.data.local.mapper

import ar.edu.unlam.scaffoldingandroid3.data.local.entity.RouteEntity
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route

/**
 * TODO: Mapper - Conversión Route (domain) ↔ RouteEntity (data)
 * Métodos: Route.toEntity(), RouteEntity.toDomain(), List<RouteEntity>.toDomain()
 * Maneja conversión de Route.Point list a relación separada si es necesario
 */

fun Route.toEntity(): RouteEntity {
    return RouteEntity(
        id = id,
        name = name,
        points = points,
        distance = distance,
        duration = duration,
        photoUri = photoUri
    )
}

fun RouteEntity.toDomain(): Route {
    return Route(
        id = id,
        name = name,
        points = points,
        distance = distance,
        duration = duration,
        photoUri = photoUri
    )
}
