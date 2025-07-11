package ar.edu.unlam.scaffoldingandroid3.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Modelo de dominio que representa una ruta en la aplicación.
 *
 * Este modelo es parte de la capa de dominio y representa la entidad principal
 * de negocio para el seguimiento de rutas. Contiene toda la información necesaria
 * para representar una ruta completa, incluyendo sus puntos de seguimiento.
 *
 * @property id Identificador único de la ruta
 * @property name Nombre descriptivo de la ruta
 * @property points Lista de puntos que conforman la ruta
 * @property distance Distancia total de la ruta en metros
 * @property duration Duración total de la ruta en milisegundos
 */
@Parcelize
data class Route(
    val id: String,
    val name: String,
    val points: @RawValue List<Point>,
    val distance: Double,
    val duration: Long,
    val photoUri: String = "",
) : Parcelable {
    /**
     * Representa un punto específico en la ruta.
     *
     * @property latitude Latitud del punto
     * @property longitude Longitud del punto
     * @property timestamp Momento en que se registró el punto
     */
    @Parcelize
    data class Point(
        val latitude: Double,
        val longitude: Double,
        val timestamp: Long,
    ) : Parcelable
}
