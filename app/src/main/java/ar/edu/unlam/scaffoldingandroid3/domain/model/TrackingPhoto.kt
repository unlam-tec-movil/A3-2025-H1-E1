package ar.edu.unlam.scaffoldingandroid3.domain.model

/**
 * Foto capturada durante tracking - Modelo de dominio simplificado
 * Las fotos pertenecen a la ruta completa, sin metadatos GPS individuales
 */
data class TrackingPhoto(
    // Ruta local de la imagen
    val uri: String,
    // Posición en la secuencia de la ruta (0, 1, 2...)
    val orderInRoute: Int,
)
