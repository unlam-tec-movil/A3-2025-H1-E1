package ar.edu.unlam.scaffoldingandroid3.domain.model

/**
 * Entidad de dominio - Configuración del mapa
 * Configuraciones de visualización como tipo de mapa, zoom, ubicación
 *
 * @property id Identificador único de la configuración
 * @property mapType Tipo de mapa (DEFAULT, SATELLITE)
 * @property zoom Nivel de zoom del mapa
 * @property centerLatitude Latitud del centro del mapa
 * @property centerLongitude Longitud del centro del mapa
 * @property showMyLocation Indica si se muestra la ubicación actual del usuario en el mapa
 */

data class MapSettings(
    val id: Long = 0,
    val mapType: MapType,
    val zoom: Float,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val showMyLocation: Boolean,
)
