package ar.edu.unlam.scaffoldingandroid3.domain.model

/**
 * Entidad de dominio - Foto tomada durante recorrido
 * Representa una foto capturada con ubicación GPS y metadata del usuario
 *
 * @property id Identificador único de la foto
 * @property uri URI o ruta del archivo de la foto en el dispositivo
 * @property timestamp Momento en que se tomó la foto
 * @property location Ubicación GPS donde se tomó la foto
 * @property description Descripción opcional de la foto
 */

data class Photo(
    val id: Long = 0,
    val uri: String,
    val timestamp: Long,
    val location: LocationPoint,
    val description: String = "",
)
