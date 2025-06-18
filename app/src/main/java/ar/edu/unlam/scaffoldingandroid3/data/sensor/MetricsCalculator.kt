package ar.edu.unlam.scaffoldingandroid3.data.sensor

import android.location.Location
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Motor de cálculo de métricas en tiempo real para tracking
 * Compatible con CU-010 a CU-013: cálculo de todas las estadísticas requeridas
 */
@Singleton
class MetricsCalculator @Inject constructor() {

    // Lista de puntos de la ruta
    private val routePoints = mutableListOf<LocationPoint>()
    
    // Métricas acumuladas
    private var totalDistance = 0.0 // en kilómetros
    private var totalSteps = 0
    private var maxSpeed = 0.0
    private var totalElevationGain = 0.0
    private var totalElevationLoss = 0.0
    
    // Estado actual
    private var currentSpeed = 0.0
    private var currentElevation = 0.0
    private var maxElevation = 0.0
    private var minElevation = Double.MAX_VALUE
    private var lastLocation: Location? = null
    private var lastAltitude: Double? = null
    
    // Para cálculo de velocidad promedio
    private var totalMovingTime = 0L
    private var lastLocationTime = 0L
    private val speedHistory = mutableListOf<Double>()

    /**
     * Añade un nuevo punto de ubicación y actualiza métricas
     */
    fun addLocationPoint(location: Location) {
        val currentTime = System.currentTimeMillis()
        
        // Crear LocationPoint para el dominio
        val locationPoint = LocationPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = if (location.hasAltitude()) location.altitude else null,
            accuracy = location.accuracy,
            speed = if (location.hasSpeed()) location.speed else null,
            timestamp = currentTime
        )
        
        routePoints.add(locationPoint)
        
        // Calcular distancia desde último punto
        lastLocation?.let { prevLocation ->
            val distance = prevLocation.distanceTo(location) / 1000.0 // convertir a km
            
            // Solo agregar si el movimiento es significativo (> 1 metro)
            if (distance > 0.001) {
                totalDistance += distance
                
                // Calcular velocidad instantánea
                val timeDiff = (currentTime - lastLocationTime) / 1000.0 // segundos
                if (timeDiff > 0) {
                    currentSpeed = (distance / timeDiff) * 3600.0 // km/h
                    
                    // Solo considerar para promedio si está en movimiento
                    if (currentSpeed > MIN_MOVING_SPEED) {
                        speedHistory.add(currentSpeed)
                        totalMovingTime += (currentTime - lastLocationTime)
                        
                        // Actualizar velocidad máxima
                        if (currentSpeed > maxSpeed) {
                            maxSpeed = currentSpeed
                        }
                    }
                }
            }
        }
        
        lastLocation = location
        lastLocationTime = currentTime
    }

    /**
     * Actualiza la altitud desde el barómetro
     */
    fun updateAltitude(altitude: Double) {
        // Calcular desniveles
        lastAltitude?.let { prevAltitude ->
            val elevationChange = altitude - prevAltitude
            
            if (elevationChange > ELEVATION_THRESHOLD) {
                totalElevationGain += elevationChange
            } else if (elevationChange < -ELEVATION_THRESHOLD) {
                totalElevationLoss += abs(elevationChange)
            }
        }
        
        currentElevation = altitude
        
        // Actualizar máximos y mínimos
        if (altitude > maxElevation) {
            maxElevation = altitude
        }
        if (altitude < minElevation) {
            minElevation = altitude
        }
        
        lastAltitude = altitude
    }

    /**
     * Actualiza el conteo de pasos
     */
    fun updateStepCount(steps: Int) {
        totalSteps = steps
    }

    /**
     * Calcula calorías quemadas basado en pasos y peso estimado
     */
    private fun calculateCalories(): Double {
        // Fórmula aproximada: 0.04 calorías por paso para persona de 70kg
        return totalSteps * CALORIES_PER_STEP
    }

    /**
     * Calcula velocidad promedio considerando solo tiempo en movimiento
     */
    private fun calculateAverageSpeed(): Double {
        return if (speedHistory.isNotEmpty()) {
            speedHistory.average()
        } else {
            0.0
        }
    }

    /**
     * Obtiene el último punto de ubicación
     */
    fun getLastLocation(): LocationPoint? {
        return routePoints.lastOrNull()
    }

    /**
     * Obtiene todas las métricas actuales
     * Compatible con CU-013: "se muestran todas las métricas detalladas"
     */
    fun getCurrentMetrics(): TrackingMetrics {
        return TrackingMetrics(
            currentSpeed = currentSpeed,
            averageSpeed = calculateAverageSpeed(),
            maxSpeed = maxSpeed,
            currentDistance = totalDistance,
            currentDuration = if (totalMovingTime > 0) totalMovingTime else 0L,
            currentElevation = currentElevation,
            totalElevationGain = totalElevationGain,
            totalElevationLoss = totalElevationLoss,
            lastLocation = getLastLocation()
        )
    }

    /**
     * Obtiene estadísticas adicionales
     */
    fun getAdditionalStats(): Map<String, Any> {
        return mapOf(
            "totalSteps" to totalSteps,
            "caloriesBurned" to calculateCalories(),
            "maxElevation" to maxElevation,
            "minElevation" to if (minElevation == Double.MAX_VALUE) 0.0 else minElevation,
            "totalElevationChange" to (maxElevation - if (minElevation == Double.MAX_VALUE) 0.0 else minElevation),
            "routePointsCount" to routePoints.size,
            "movingTimeMinutes" to (totalMovingTime / 60000.0) // minutos
        )
    }

    /**
     * Obtiene todos los puntos de la ruta
     */
    fun getAllRoutePoints(): List<LocationPoint> {
        return routePoints.toList()
    }

    /**
     * Resetea todas las métricas (para nueva sesión)
     */
    fun reset() {
        routePoints.clear()
        totalDistance = 0.0
        totalSteps = 0
        maxSpeed = 0.0
        totalElevationGain = 0.0
        totalElevationLoss = 0.0
        currentSpeed = 0.0
        currentElevation = 0.0
        maxElevation = 0.0
        minElevation = Double.MAX_VALUE
        lastLocation = null
        lastAltitude = null
        totalMovingTime = 0L
        lastLocationTime = 0L
        speedHistory.clear()
    }

    /**
     * Calcula la distancia total de la ruta
     */
    fun getTotalDistance(): Double = totalDistance

    /**
     * Calcula el tiempo total en movimiento
     */
    fun getMovingTime(): Long = totalMovingTime

    /**
     * Verifica si actualmente está en movimiento
     */
    fun isMoving(): Boolean = currentSpeed > MIN_MOVING_SPEED

    /**
     * Obtiene el pace actual (minutos por kilómetro)
     */
    fun getCurrentPace(): Double {
        return if (currentSpeed > 0) {
            60.0 / currentSpeed // min/km
        } else {
            0.0
        }
    }

    /**
     * Obtiene el pace promedio
     */
    fun getAveragePace(): Double {
        val avgSpeed = calculateAverageSpeed()
        return if (avgSpeed > 0) {
            60.0 / avgSpeed // min/km
        } else {
            0.0
        }
    }

    companion object {
        private const val MIN_MOVING_SPEED = 0.5 // km/h - velocidad mínima para considerar movimiento
        private const val ELEVATION_THRESHOLD = 0.5 // metros - umbral para cambios de elevación
        private const val CALORIES_PER_STEP = 0.04 // calorías por paso (estimación para 70kg)
    }
}