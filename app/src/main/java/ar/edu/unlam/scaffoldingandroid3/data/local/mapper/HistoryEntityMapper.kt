package ar.edu.unlam.scaffoldingandroid3.data.local.mapper

import ar.edu.unlam.scaffoldingandroid3.data.local.entity.HistoryEntity
import ar.edu.unlam.scaffoldingandroid3.domain.model.History
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint
import ar.edu.unlam.scaffoldingandroid3.domain.model.Photo
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Mapper - Conversión History (domain) ↔ HistoryEntity (data)
 * Métodos: History.toEntity(), HistoryEntity.toDomain()
 * Maneja serialización de datos complejos como fotos y estadísticas
 */

private val gson = Gson()

fun History.toEntity(): HistoryEntity {
    return HistoryEntity(
        id = id,
        routeName = routeName,
        date = date,
        metricsJson = gson.toJson(metrics),
        photosJson = gson.toJson(photos),
        routePointsJson = gson.toJson(routePoint)
    )
}

fun HistoryEntity.toDomain(): History {
    return History(
        id = id,
        routeName = routeName,
        date = date,
        metrics = gson.fromJson(metricsJson, TrackingMetrics::class.java),
        photos = gson.fromJson(photosJson, object : TypeToken<List<Photo>>() {}.type),
        routePoint = gson.fromJson(routePointsJson, object : TypeToken<List<LocationPoint>>() {}.type)
    )
}
