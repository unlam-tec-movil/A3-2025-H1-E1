package ar.edu.unlam.scaffoldingandroid3.data.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * TODO: Implementación CameraService - Operaciones de cámara
 * @Inject constructor(context)
 * Implementa: capturePhoto(), hasPermission(), requestPermission()
 * Usa CameraX para captura + permisos Android + file storage
 */

class CameraServiceImpl {
    fun createImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("ROUTE_${timeStamp}_", ".jpg", storageDir)
    }

    fun getImageUri(context: Context): Uri {
        val imageFile = createImageFile(context)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile,
        )
    }
}
