package ar.edu.unlam.scaffoldingandroid3.domain.model

/**
 * TODO: Entidad de dominio - Punto GPS individual
 * Representa coordenadas GPS con timestamp y datos de precisión/velocidad
 * LocationPoint tendría propiedades extra para tracking GPS
 */
data class LocationPoint(
    // Precisión GPS (importante para filtering)
    val accuracy: Float,
    // Velocidad actual (para métricas)
    val speed: Float?,
    // Altitud (para gráficos elevación)
    val altitude: Double?,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
)
