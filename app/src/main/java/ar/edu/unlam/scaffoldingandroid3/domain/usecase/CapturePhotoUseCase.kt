package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingPhoto
import ar.edu.unlam.scaffoldingandroid3.domain.repository.CameraRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.TrackingSessionRepository
import javax.inject.Inject

/**
 * Use Case para capturar fotos durante tracking (simplificado)
 * Compatible con Clean Architecture: ViewModel → UseCase → Repository
 * Las fotos se vinculan automáticamente a la ruta en curso
 */
class CapturePhotoUseCase
    @Inject
    constructor(
        private val cameraRepository: CameraRepository,
        private val trackingSessionRepository: TrackingSessionRepository,
    ) {
        /**
         * Captura una foto y la vincula a la ruta actual
         * Sin metadatos GPS individuales - más simple y directo
         */
        suspend fun execute(currentPhotoCount: Int): Result<TrackingPhoto> {
            return try {
                // Verificar que hay una sesión activa
                val currentSession =
                    trackingSessionRepository.getCurrentTrackingSession()
                        ?: return Result.failure(Exception("No hay sesión de tracking activa"))

                // Verificar permisos de cámara
                if (!cameraRepository.hasCameraPermission()) {
                    return Result.failure(Exception("Permiso de cámara no concedido"))
                }

                // Capturar la foto
                val photoUriResult = cameraRepository.capturePhoto()
                if (photoUriResult.isFailure) {
                    return Result.failure(photoUriResult.exceptionOrNull() ?: Exception("Error al capturar foto"))
                }

                val photoUri = photoUriResult.getOrThrow()

                // Crear TrackingPhoto simplificada - solo URI y orden
                val trackingPhoto =
                    cameraRepository.createTrackingPhoto(
                        imageUri = photoUri,
                        orderInRoute = currentPhotoCount,
                    )

                Result.success(trackingPhoto)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        /**
         * Verifica si se puede capturar una foto
         */
        suspend fun canCapturePhoto(): Boolean {
            val hasPermission = cameraRepository.hasCameraPermission()
            val hasActiveSession = trackingSessionRepository.getCurrentTrackingSession() != null
            return hasPermission && hasActiveSession
        }
    }
