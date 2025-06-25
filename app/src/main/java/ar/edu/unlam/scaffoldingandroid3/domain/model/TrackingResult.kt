package ar.edu.unlam.scaffoldingandroid3.domain.model

/**
 * Resultado final de tracking para guardar - Modelo de dominio
 */
data class TrackingResult(
    val tiempoTotal: String, // Tiempo total con pausas
    val tiempoEnMovimiento: String, // Tiempo sin pausas
    val distanciaTotal: Double, // km totales
    val pasosTotales: Int, // pasos totales
    val velocidadMedia: Double, // km/h promedio
    val velocidadMaxima: Double, // km/h máxima
    val altitudMinima: Double, // metros, punto más bajo
    val altitudMaxima: Double, // metros, punto más alto
    val rutaCompleta: List<LocationPoint>, // todos los puntos GPS
    val fotosCapturadas: List<TrackingPhoto>, // todas las fotos
    var nombreRecorrido: String = "", // ingresado por usuario
    val fechaCreacion: Long, // timestamp de creación
)