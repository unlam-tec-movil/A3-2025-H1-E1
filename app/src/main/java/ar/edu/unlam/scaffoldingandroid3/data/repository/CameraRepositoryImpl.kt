package ar.edu.unlam.scaffoldingandroid3.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingPhoto
import ar.edu.unlam.scaffoldingandroid3.domain.repository.CameraRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del CameraRepository - Adaptador para cámara Android
 * Conecta dominio con servicios de cámara Android
 * Respeta Clean Architecture: Domain → Data → Android Camera
 */
@Singleton
class CameraRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CameraRepository {
    
    // Canal para comunicación con Activity Result API
    private val captureResultChannel = Channel<Result<String>>(Channel.UNLIMITED)
    
    override suspend fun capturePhoto(): Result<String> {
        return try {
            // Crear archivo para la foto
            val photoFile = createImageFile()
            val photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            
            // En implementación real, esto activaría Activity Result API
            // Por ahora simulamos éxito para que funcione la integración
            Result.success(photoUri.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createTrackingPhoto(
        imageUri: String,
        orderInRoute: Int
    ): TrackingPhoto {
        return TrackingPhoto(
            uri = imageUri,
            orderInRoute = orderInRoute
        )
    }
    
    override fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    override suspend fun requestCameraPermission(): Boolean {
        // En este caso simple, asumimos que el permiso ya está concedido
        // En implementación completa, usaríamos Activity Result API
        return hasCameraPermission()
    }
    
    private fun createImageFile(): File {
        // Crear nombre único para la imagen
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "TRACKING_${timeStamp}_"
        val storageDir = File(context.getExternalFilesDir(null), "tracking_photos")
        
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }
}