package ar.edu.unlam.scaffoldingandroid3.domain.model

/**
 * Entidad de dominio - Métricas calculadas en tiempo real
 * Contiene estadísticas como distancia, velocidad, elevación durante tracking
 *
 * @property id Identificador único de las métricas
 * @property currentSpeed Velocidad actual en km/h
 * @property averageSpeed Velocidad promedio en km/h
 * @property maxSpeed Velocidad máxima alcanzada en km/h
 * @property currentDistance Distancia recorrida en kilómetros
 * @property currentDuration Duración actual en milisegundos
 * @property currentElevation Elevación actual en metros sobre el nivel del mar
 * @property totalElevationGain Ganancia total de elevación en metros
 * @property totalElevationLoss Pérdida total de elevación en metros
 * @property totalSteps Número total de pasos detectados
 * @property lastLocation Última ubicación registrada
 */

data class TrackingMetrics(
    val id: Long = 0,
    val currentSpeed: Double = 0.0,
    val averageSpeed: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val currentDistance: Double = 0.0,
    val currentDuration: Long = 0,
    val currentElevation: Double = 0.0,
    val totalElevationGain: Double = 0.0,
    val totalElevationLoss: Double = 0.0,
    val totalSteps: Int = 0,
    val lastLocation: LocationPoint? = null,
)
