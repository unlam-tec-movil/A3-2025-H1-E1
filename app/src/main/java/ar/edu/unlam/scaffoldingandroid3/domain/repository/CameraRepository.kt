package ar.edu.unlam.scaffoldingandroid3.domain.repository

import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingPhoto

/**
 * Puerto de salida - Gestión de cámara
 * Compatible con Clean Architecture: Domain → Data → Android Camera
 */
interface CameraRepository {
    /**
     * Lanza la cámara para capturar una foto
     * Devuelve la URI de la imagen capturada
     */
    suspend fun capturePhoto(): Result<String>

    /**
     * Crea una TrackingPhoto simplificada (sin metadatos GPS)
     */
    suspend fun createTrackingPhoto(
        imageUri: String,
        orderInRoute: Int,
    ): TrackingPhoto

    /**
     * Verifica si el permiso de cámara está concedido
     */
    fun hasCameraPermission(): Boolean

    /**
     * Solicita permiso de cámara
     */
    suspend fun requestCameraPermission(): Boolean
}
