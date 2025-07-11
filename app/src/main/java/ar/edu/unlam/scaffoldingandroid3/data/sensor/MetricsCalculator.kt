package ar.edu.unlam.scaffoldingandroid3.data.sensor

import android.location.Location
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calculadora de métricas en tiempo real
 */
@Singleton
class MetricsCalculator
    @Inject
    constructor() {
        // Variables básicas
        private var totalDistance = 0.0 // en kilómetros
        private var maxSpeed = 0.0
        private var currentSpeed = 0.0
        private var currentElevation = 0.0
        private var lastLocation: LocationPoint? = null
        private var lastLocationTime = 0L

        // Variables para sensores
        private var totalSteps = 0
        private var lastAltitude: Double? = null

        // Lista de puntos de la ruta
        private val routePoints = mutableListOf<LocationPoint>()

        // Historial de altitudes para min/max
        private val altitudeHistory = mutableListOf<Double>()

        // Variables para cálculo correcto de velocidad media
        private var totalMovementTime = 0L // tiempo EN MOVIMIENTO sin pausas (milisegundos)

        // Control de pausas para evitar líneas falsas en el mapa
        private var isPaused = false

        // Tiempo de inicio para cronómetro de UI (independiente del GPS)
        private var startTime: Long = 0L
        private var pausedDuration: Long = 0L
        private var pauseStartTime: Long = 0L

        /**
         * Actualiza ubicación GPS
         */
        fun addLocationPoint(location: Location) {
            // No agregar puntos si está pausado
            if (isPaused) return

            val currentTime = System.currentTimeMillis()

            // Crear punto de dominio
            val locationPoint =
                LocationPoint(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = if (location.hasAltitude()) location.altitude else null,
                    accuracy = location.accuracy,
                    speed = if (location.hasSpeed()) location.speed else null,
                    timestamp = currentTime,
                )

            // Calcular distancia simple
            lastLocation?.let { prev ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    prev.latitude,
                    prev.longitude,
                    location.latitude,
                    location.longitude,
                    results,
                )

                val distance = results[0] / 1000.0 // convertir a km
                val timeDiff = (currentTime - lastLocationTime) / 1000.0 // segundos

                // Filtrado para distancia más precisa
                val accuracyThreshold = 15.0f // 15m - Solo GPS preciso (evita inflación)
                val speedThreshold = 50.0 // Filtrar saltos GPS > 50 km/h (más realista para caminata)
                val minDistance = 0.005 // 5m - Balance entre precisión y funcionalidad
                val maxTimeDiff = 5.0 // 5s - Mantiene responsividad sin ser excesivo

                val instantSpeed = if (timeDiff > 0) (distance / timeDiff) * 3600.0 else 0.0
                val hasGoodAccuracy = location.accuracy <= accuracyThreshold
                val isReasonableSpeed = instantSpeed <= speedThreshold
                val hasSignificantMovement = distance >= minDistance
                val hasTimedOut = timeDiff >= maxTimeDiff

                // Aceptar puntos buenos aunque tengan poca distancia
                val shouldAddPoint = hasGoodAccuracy && isReasonableSpeed && (hasSignificantMovement || hasTimedOut)

                if (shouldAddPoint) {
                    totalDistance += distance

                    // Agregar punto a la ruta
                    routePoints.add(locationPoint)

                    // Calcular velocidad instantánea
                    if (timeDiff > 0) {
                        currentSpeed = (distance / timeDiff) * 3600.0 // km/h
                        if (currentSpeed > maxSpeed) {
                            maxSpeed = currentSpeed
                        }

                        // Acumular tiempo de movimiento para velocidad media correcta
                        totalMovementTime += (currentTime - lastLocationTime)
                    }
                }
            }

            lastLocation = locationPoint
            lastLocationTime = currentTime
        }

        /**
         * Actualiza altitud del barómetro
         */
        fun updateAltitude(altitude: Double) {
            currentElevation = altitude
            lastAltitude = altitude

            // Agregar al historial para cálculos min/max
            altitudeHistory.add(altitude)
        }

        /**
         * Actualiza conteo de pasos
         */
        fun updateStepCount(steps: Int) {
            totalSteps = steps
        }

        /**
         * Obtiene métricas actuales
         */
        fun getCurrentMetrics(): TrackingMetrics {
            // Calcular velocidad media correcta: distancia / tiempo en movimiento
            val averageSpeed =
                if (totalDistance > 0 && totalMovementTime > 0) {
                    (totalDistance / (totalMovementTime / 3600000.0)) // convertir ms a horas
                } else {
                    0.0
                }

            // Tiempo para cronómetro UI: tiempo continuo desde el inicio excluyendo pausas
            val elapsedTimeForUI =
                if (startTime > 0) {
                    if (isPaused) {
                        // Durante pausa: tiempo hasta que se pausó
                        pauseStartTime - startTime - pausedDuration
                    } else {
                        // Activo: tiempo actual menos tiempo pausado
                        System.currentTimeMillis() - startTime - pausedDuration
                    }
                } else {
                    0L
                }

            return TrackingMetrics(
                currentSpeed = currentSpeed,
                averageSpeed = averageSpeed,
                maxSpeed = maxSpeed,
                currentDistance = totalDistance,
                // Tiempo continuo para UI
                currentDuration = elapsedTimeForUI,
                currentElevation = currentElevation,
                minElevation = getMinAltitude(),
                maxElevation = getMaxAltitude(),
                totalSteps = totalSteps,
                lastLocation = lastLocation,
            )
        }

        /**
         * Obtiene los puntos de la ruta en tiempo real para pintar el camino
         */
        fun getCurrentRoutePoints(): List<LocationPoint> {
            return routePoints.toList() // Copia segura para UI
        }

        /**
         * Estadísticas adicionales con altitudes min/max
         */
        fun getAdditionalStats(): Map<String, Any> {
            return mapOf(
                "totalSteps" to totalSteps,
                "totalDistance" to totalDistance,
                "maxSpeed" to maxSpeed,
                "minAltitude" to getMinAltitude(),
                "maxAltitude" to getMaxAltitude(),
                "totalMovementTime" to totalMovementTime,
            )
        }

        /**
         * Obtiene la altitud mínima registrada
         */
        fun getMinAltitude(): Double {
            return altitudeHistory.minOrNull() ?: currentElevation
        }

        /**
         * Obtiene la altitud máxima registrada
         */
        fun getMaxAltitude(): Double {
            return altitudeHistory.maxOrNull() ?: currentElevation
        }

        /**
         * Obtiene todos los puntos de la ruta
         */
        fun getAllRoutePoints(): List<LocationPoint> {
            return routePoints.toList()
        }

        /**
         * Pausa el tracking (no agrega más puntos a la ruta)
         */
        fun pauseTracking() {
            if (!isPaused) {
                isPaused = true
                pauseStartTime = System.currentTimeMillis()
                // Resetear última ubicación para evitar líneas falsas al reanudar
                lastLocation = null
                lastLocationTime = 0L
            }
        }

        /**
         * Reanuda el tracking (vuelve a agregar puntos)
         */
        fun resumeTracking() {
            if (isPaused) {
                isPaused = false
                // Acumular tiempo pausado
                pausedDuration += System.currentTimeMillis() - pauseStartTime
                // Al reanudar, la primera ubicación será un nuevo inicio
                // Esto crea el gap visual correcto
            }
        }

        /**
         * Resetea todas las métricas
         */
        fun reset() {
            totalDistance = 0.0
            maxSpeed = 0.0
            currentSpeed = 0.0
            currentElevation = 0.0
            totalSteps = 0 // SÍ resetear al iniciar nueva sesión
            lastLocation = null
            lastAltitude = null
            lastLocationTime = 0L
            totalMovementTime = 0L
            routePoints.clear()
            altitudeHistory.clear()
            isPaused = false
            startTime = System.currentTimeMillis() // Inicializar cronómetro
            pausedDuration = 0L
            pauseStartTime = 0L
        }
    }
