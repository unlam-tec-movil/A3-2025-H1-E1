package ar.edu.unlam.scaffoldingandroid3.domain.model

/**
 * TODO: Entidad de dominio - Punto GPS individual
 * Representa coordenadas GPS con timestamp y datos de precisión/velocidad
 */

// LocationPoint tendría propiedades extra para tracking GPS
data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val accuracy: Float,      // ← Precisión GPS (importante para filtering)
    val speed: Float?,        // ← Velocidad actual (para métricas)
    val altitude: Double?     // ← Altitud (para gráficos elevación)
)
