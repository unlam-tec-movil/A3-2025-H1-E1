package ar.edu.unlam.scaffoldingandroid3.domain.logic

import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Lógica de dominio para cálculos de visualización de rutas
 * Centraliza la lógica de duración estimada y conversión de unidades
 */
class RouteDisplayCalculator
    @Inject
    constructor() {
        /**
         * Calcula la duración estimada de una ruta basada en velocidad promedio de trekking
         *
         * Velocidades promedio consideradas:
         * - Terreno plano: 4-5 km/h
         * - Terreno montañoso: 2-3 km/h
         * - Promedio general para trekking: 3.5 km/h
         */
        fun calculateEstimatedDuration(route: Route): Long {
            // Si ya tiene duración definida, usarla
            if (route.duration > 0) {
                return route.duration
            }

            val distanceKm = getDistanceInKilometers(route)

            // Velocidad promedio de trekking: 3.5 km/h
            // Incluye pausas para descanso, fotos, etc.
            val averageSpeedKmh = 3.5

            // Calcular tiempo en horas
            val timeHours = distanceKm / averageSpeedKmh

            // Convertir a milisegundos
            return (timeHours * 60 * 60 * 1000).roundToInt().toLong()
        }

        /**
         * Obtiene la distancia en metros, manejando diferentes tipos de ruta
         */
        fun getDistanceInMeters(route: Route): Double {
            return when (getRouteType(route)) {
                RouteType.USER_TRACKING -> route.distance * 1000 // km a metros
                RouteType.API_ORIGINAL, RouteType.API_SAVED -> route.distance // ya en metros
            }
        }

        /**
         * Obtiene la distancia en kilómetros, manejando diferentes tipos de ruta
         */
        fun getDistanceInKilometers(route: Route): Double {
            return when (getRouteType(route)) {
                RouteType.USER_TRACKING -> route.distance // ya en km
                RouteType.API_ORIGINAL, RouteType.API_SAVED -> route.distance / 1000 // metros a km
            }
        }

        /**
         * Determina el tipo de ruta basado en su ID
         */
        private fun getRouteType(route: Route): RouteType {
            return when {
                route.id.startsWith("api-") -> RouteType.API_SAVED
                route.id.contains("-") -> RouteType.USER_TRACKING // UUID
                else -> RouteType.API_ORIGINAL // ID numérico
            }
        }

        /**
         * Formatea la duración en un string legible
         */
        fun formatDuration(durationMillis: Long): String {
            val totalMinutes = (durationMillis / (1000 * 60)).toInt()

            return when {
                totalMinutes < 60 -> "${totalMinutes}min"
                totalMinutes % 60 == 0 -> "${totalMinutes / 60}h"
                else -> {
                    val hours = totalMinutes / 60
                    val minutes = totalMinutes % 60
                    "${hours}h ${minutes}min"
                }
            }
        }
    }

/**
 * Tipos de ruta según origen
 */
private enum class RouteType {
    USER_TRACKING, // Rutas creadas por tracking del usuario (UUID)
    API_ORIGINAL, // Rutas de API Overpass (ID numérico)
    API_SAVED, // Rutas de API guardadas localmente (prefijo "api-")
}
