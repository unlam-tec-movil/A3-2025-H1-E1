package ar.edu.unlam.scaffoldingandroid3.domain.model

/**
 * Entidad de dominio - Sesión de tracking GPS activa
 * Representa una sesión de grabación de ruta en tiempo real con estado y métricas
 *
 * @property id Identificador único de la sesión
 * @property routeName Nombre de la ruta asociada a la sesión
 * @property startTime Momento de inicio de la sesión
 * @property endTime Momento de finalización de la sesión
 * @property metrics Métricas calculadas durante la sesión (velocidad, distancia, etc.)
 * @property status Estado actual de la sesión (ACTIVE, PAUSED, COMPLETED)
 * @property routePoint Lista de puntos de la ruta asociada a la sesión
 */

data class TrackingSession(
    val id: Long = 0,
    val routeName: String,
    val startTime: Long,
    val endTime: Long,
    val metrics: TrackingMetrics,
    val status: TrackingStatus,
    val routePoint: List<LocationPoint>,
    val photo: String
)
