package ar.edu.unlam.scaffoldingandroid3.domain.model

/**
 * Entidad de dominio - Actividad completada en el historial
 * Representa una ruta ya finalizada con estadísticas completas y fotos
 */

data class History(
    val id: Long = 0,
    val routeName: String,
    val date: String,
    val metrics: TrackingMetrics,
    val photos: List<Photo>,
    val routePoint: List<LocationPoint>,
)
