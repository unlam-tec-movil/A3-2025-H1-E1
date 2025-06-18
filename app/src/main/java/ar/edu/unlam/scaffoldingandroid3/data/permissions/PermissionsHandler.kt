package ar.edu.unlam.scaffoldingandroid3.data.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de permisos para la aplicación de senderismo
 * Compatible con CU-037 y CU-038: inicialización y manejo de permisos
 */
@Singleton
class PermissionsHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Permisos requeridos para funcionalidad básica
     */
    val basicPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA
    )

    /**
     * Permisos requeridos para funcionalidad completa (incluyendo background)
     */
    val allPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    /**
     * Verifica si todos los permisos básicos están otorgados
     */
    fun hasBasicPermissions(): Boolean {
        return basicPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Verifica si todos los permisos están otorgados
     */
    fun hasAllPermissions(): Boolean {
        return allPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Verifica permisos específicos de ubicación
     */
    fun hasLocationPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val coarseLocation = ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        return fineLocation && coarseLocation
    }

    /**
     * Verifica permiso de ubicación en background
     */
    fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No requerido en versiones anteriores
        }
    }

    /**
     * Verifica permiso de cámara
     */
    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Verifica permisos de almacenamiento
     */
    fun hasStoragePermissions(): Boolean {
        val writePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        
        val readPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        
        return writePermission && readPermission
    }

    /**
     * Solicita permisos básicos
     */
    fun requestBasicPermissions(activity: Activity, requestCode: Int = REQUEST_BASIC_PERMISSIONS) {
        val permissionsToRequest = basicPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsToRequest, requestCode)
        }
    }

    /**
     * Solicita permiso de ubicación en background (debe llamarse después de obtener permisos básicos)
     */
    fun requestBackgroundLocationPermission(activity: Activity, requestCode: Int = REQUEST_BACKGROUND_LOCATION) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasBackgroundLocationPermission()) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                requestCode
            )
        }
    }

    /**
     * Solicita todos los permisos
     */
    fun requestAllPermissions(activity: Activity, requestCode: Int = REQUEST_ALL_PERMISSIONS) {
        val permissionsToRequest = allPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsToRequest, requestCode)
        }
    }

    /**
     * Verifica si se debe mostrar explicación para un permiso
     */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * Obtiene permisos faltantes
     */
    fun getMissingPermissions(): List<String> {
        return allPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Obtiene el estado de funcionalidades basado en permisos
     */
    fun getFeatureAvailability(): Map<String, Boolean> {
        return mapOf(
            "basic_mapping" to hasLocationPermissions(),
            "gps_tracking" to hasLocationPermissions(),
            "background_tracking" to (hasLocationPermissions() && hasBackgroundLocationPermission()),
            "camera" to hasCameraPermission(),
            "photo_storage" to hasStoragePermissions(),
            "full_functionality" to hasAllPermissions()
        )
    }

    /**
     * Procesa el resultado de solicitud de permisos
     */
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): PermissionResult {
        val granted = mutableListOf<String>()
        val denied = mutableListOf<String>()

        permissions.forEachIndexed { index, permission ->
            if (index < grantResults.size) {
                if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                    granted.add(permission)
                } else {
                    denied.add(permission)
                }
            }
        }

        return PermissionResult(
            requestCode = requestCode,
            granted = granted,
            denied = denied,
            hasAllBasicPermissions = hasBasicPermissions(),
            hasAllPermissions = hasAllPermissions()
        )
    }

    /**
     * Obtiene mensajes explicativos para permisos
     */
    fun getPermissionExplanation(permission: String): String {
        return when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION -> 
                "La aplicación necesita acceso a la ubicación para rastrear tu ruta durante el senderismo y mostrar tu posición en el mapa."
            
            Manifest.permission.ACCESS_BACKGROUND_LOCATION ->
                "Para continuar grabando tu ruta cuando la app está en segundo plano, necesitamos permiso de ubicación en background."
            
            Manifest.permission.CAMERA ->
                "La cámara es necesaria para tomar fotos durante tus recorridos y asociarlas con la ubicación GPS."
            
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE ->
                "Se necesita acceso al almacenamiento para guardar las fotos de tus recorridos y datos de rutas."
            
            else -> "Este permiso es necesario para el correcto funcionamiento de la aplicación."
        }
    }

    companion object {
        const val REQUEST_BASIC_PERMISSIONS = 1001
        const val REQUEST_BACKGROUND_LOCATION = 1002
        const val REQUEST_ALL_PERMISSIONS = 1003
        const val REQUEST_CAMERA_PERMISSION = 1004
        const val REQUEST_STORAGE_PERMISSION = 1005
    }
}

/**
 * Resultado del procesamiento de permisos
 */
data class PermissionResult(
    val requestCode: Int,
    val granted: List<String>,
    val denied: List<String>,
    val hasAllBasicPermissions: Boolean,
    val hasAllPermissions: Boolean
) {
    val isSuccessful: Boolean = denied.isEmpty()
    val hasLocationPermissions: Boolean = granted.any { 
        it == Manifest.permission.ACCESS_FINE_LOCATION || it == Manifest.permission.ACCESS_COARSE_LOCATION 
    }
}