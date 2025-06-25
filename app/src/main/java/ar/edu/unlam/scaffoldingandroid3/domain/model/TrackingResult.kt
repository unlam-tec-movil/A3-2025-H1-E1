package ar.edu.unlam.scaffoldingandroid3.domain.model

/**
 * Resultado final de tracking para guardar - Modelo de dominio
 */
data class TrackingResult(
    // Tiempo total con pausas
    val tiempoTotal: String,
    // Tiempo sin pausas
    val tiempoEnMovimiento: String,
    // km totales
    val distanciaTotal: Double,
    // pasos totales
    val pasosTotales: Int,
    // km/h promedio
    val velocidadMedia: Double,
    // km/h máxima
    val velocidadMaxima: Double,
    // metros, punto más bajo
    val altitudMinima: Double,
    // metros, punto más alto
    val altitudMaxima: Double,
    // todos los puntos GPS
    val rutaCompleta: List<LocationPoint>,
    // todas las fotos
    val fotosCapturadas: List<TrackingPhoto>,
    // ingresado por usuario
    var nombreRecorrido: String = "",
    // timestamp de creación
    val fechaCreacion: Long,
)
