package ar.edu.unlam.scaffoldingandroid3.data.sensor

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Servicio en foreground para tracking continuo de GPS y sensores
 * Compatible con CU-010 a CU-014: manejo de estados start/pause/resume/stop
 */
@AndroidEntryPoint
class TrackingService : Service() {
    @Inject lateinit var locationService: LocationService

    @Inject lateinit var sensorManager: DeviceSensorManager

    @Inject lateinit var metricsCalculator: MetricsCalculator

    @Inject lateinit var trackingSessionRepository: ar.edu.unlam.scaffoldingandroid3.data.repository.TrackingSessionRepositoryImpl

    // Flag para evitar múltiples llamadas a startForeground
    private var isStarted = false

    private val binder = TrackingBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Estados del servicio
    private val _trackingStatus = MutableStateFlow(TrackingStatus.COMPLETED)
    val trackingStatus: StateFlow<TrackingStatus> = _trackingStatus.asStateFlow()

    // Simplificado: solo location, el resto va via MetricsCalculator
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    // Jobs para cancelar flows cuando sea necesario
    private var locationJob: Job? = null
    private var sensorJobs: List<Job> = emptyList()

    private var startTime: Long = 0
    private var pausedDuration: Long = 0
    private var pauseStartTime: Long = 0

    inner class TrackingBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        // IMPORTANTE: Llamar startForeground() INMEDIATAMENTE para evitar crash
        // Solo para ACTION_START_TRACKING (otras acciones ya están en foreground)
        if (intent?.action == ACTION_START_TRACKING && !isStarted) {
            try {
                // Verificar permisos críticos antes de startForeground
                val hasLocationPermission =
                    ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED

                if (!hasLocationPermission) {
                    // Sin permisos de ubicación, no podemos hacer tracking
                    stopSelf()
                    return START_NOT_STICKY
                }

                startForeground(NOTIFICATION_ID, createNotification())
                isStarted = true
            } catch (e: SecurityException) {
                // Error de permisos
                stopSelf()
                return START_NOT_STICKY
            } catch (e: Exception) {
                // Cualquier otro error en startForeground
                stopSelf()
                return START_NOT_STICKY
            }
        }

        when (intent?.action) {
            ACTION_START_TRACKING -> startTracking()
            ACTION_PAUSE_TRACKING -> pauseTracking()
            ACTION_RESUME_TRACKING -> resumeTracking()
            ACTION_STOP_TRACKING -> stopTracking()
        }
        return START_STICKY
    }

    /**
     * Inicia el tracking (CU-010: Comenzar grabación de ruta)
     */
    fun startTracking() {
        if (_trackingStatus.value != TrackingStatus.COMPLETED) return

        _trackingStatus.value = TrackingStatus.ACTIVE
        startTime = System.currentTimeMillis()
        pausedDuration = 0

        // Resetear TODO para nueva sesión (esto es CORRECTO al iniciar nueva sesión)
        sensorManager.resetStepCount() // Resetear pasos para nueva sesión
        metricsCalculator.reset() // Resetear métricas para nueva sesión

        startLocationTracking()
        startSensorTracking()
    }

    /**
     * Pausa el tracking (CU-011: Pausar grabación de ruta)
     */
    fun pauseTracking() {
        if (_trackingStatus.value != TrackingStatus.ACTIVE) return

        _trackingStatus.value = TrackingStatus.PAUSED
        pauseStartTime = System.currentTimeMillis()

        // Pausar GPS y sensores
        locationJob?.cancel()
        sensorJobs.forEach { it.cancel() }
        sensorManager.pauseStepTracking()
        metricsCalculator.pauseTracking() // Pausar tracking de ruta

        updateNotification()
    }

    /**
     * Reanuda el tracking (CU-012: Reanudar grabación pausada)
     */
    fun resumeTracking() {
        if (_trackingStatus.value != TrackingStatus.PAUSED) return

        _trackingStatus.value = TrackingStatus.ACTIVE
        pausedDuration += System.currentTimeMillis() - pauseStartTime

        // Reanudar GPS y sensores
        metricsCalculator.resumeTracking() // Reanudar tracking de ruta
        startLocationTracking()
        startSensorTracking()
        sensorManager.resumeStepTracking()

        updateNotification()
    }

    /**
     * Detiene el tracking (CU-014: Finalizar grabación de ruta)
     */
    fun stopTracking() {
        _trackingStatus.value = TrackingStatus.COMPLETED

        // Cancelar todos los jobs
        locationJob?.cancel()
        sensorJobs.forEach { it.cancel() }

        stopForeground(STOP_FOREGROUND_REMOVE)
        isStarted = false
        stopSelf()
    }

    private fun startLocationTracking() {
        locationJob =
            serviceScope.launch {
                try {
                    locationService.getLocationUpdates()
                        .catch { /* GPS Error */ }
                        .collect { location ->
                            _currentLocation.value = location
                            metricsCalculator.addLocationPoint(location)
                            // Actualizar métricas en repository después de cada ubicación
                            trackingSessionRepository.updateMetrics(metricsCalculator.getCurrentMetrics())
                        }
                } catch (e: Exception) {
                    // Error in location tracking
                }
            }
    }

    private fun startSensorTracking() {
        sensorJobs =
            listOf(
                // Tracking de pasos - solo a MetricsCalculator
                serviceScope.launch {
                    try {
                        sensorManager.getStepUpdates()
                            .collect { steps ->
                                metricsCalculator.updateStepCount(steps)
                                // Actualizar métricas en repository después de cada paso
                                trackingSessionRepository.updateMetrics(metricsCalculator.getCurrentMetrics())
                            }
                    } catch (e: Exception) {
                        // Error in step tracking
                    }
                },
                // Tracking de altitud - solo a MetricsCalculator
                serviceScope.launch {
                    try {
                        sensorManager.getAltitudeUpdates()
                            .collect { altitude ->
                                metricsCalculator.updateAltitude(altitude)
                                // Actualizar métricas en repository después de cada altitud
                                trackingSessionRepository.updateMetrics(metricsCalculator.getCurrentMetrics())
                            }
                    } catch (e: Exception) {
                        // Error in altitude tracking
                    }
                },
            )
    }

    /**
     * Obtiene el tiempo EN MOVIMIENTO (sin pausas)
     */
    fun getMovementTime(): Long {
        return when (_trackingStatus.value) {
            TrackingStatus.ACTIVE -> System.currentTimeMillis() - startTime - pausedDuration
            TrackingStatus.PAUSED -> pauseStartTime - startTime - pausedDuration
            TrackingStatus.COMPLETED -> {
                // Mantener tiempo final, no resetear
                metricsCalculator.getAdditionalStats()["totalMovementTime"] as? Long ?: 0L
            }
        }
    }


    /**
     * DEPRECATED - Usar getMovementTime()
     */
    @Deprecated("Use getMovementTime()")
    fun getElapsedTime(): Long = getMovementTime()

    /**
     * Obtiene las métricas actuales calculadas
     */
    fun getCurrentMetrics() = metricsCalculator.getCurrentMetrics()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Tracking de Rutas",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Notificaciones durante el tracking de rutas"
                    setShowBadge(false)
                }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        // Simplificado para evitar delays en startForeground()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Senderismo - Tracking")
            .setContentText("Iniciando...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        // Verificar permiso POST_NOTIFICATIONS para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return // No se puede mostrar notificación sin permiso
            }
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        isStarted = false
    }

    companion object {
        private const val CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1

        const val ACTION_START_TRACKING = "START_TRACKING"
        const val ACTION_PAUSE_TRACKING = "PAUSE_TRACKING"
        const val ACTION_RESUME_TRACKING = "RESUME_TRACKING"
        const val ACTION_STOP_TRACKING = "STOP_TRACKING"

        fun startService(context: Context) {
            val intent =
                Intent(context, TrackingService::class.java).apply {
                    action = ACTION_START_TRACKING
                    setPackage(context.packageName)
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseService(context: Context) {
            val intent =
                Intent(context, TrackingService::class.java).apply {
                    action = ACTION_PAUSE_TRACKING
                    setPackage(context.packageName)
                }
            context.startService(intent)
        }

        fun resumeService(context: Context) {
            val intent =
                Intent(context, TrackingService::class.java).apply {
                    action = ACTION_RESUME_TRACKING
                    setPackage(context.packageName)
                }
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent =
                Intent(context, TrackingService::class.java).apply {
                    action = ACTION_STOP_TRACKING
                    setPackage(context.packageName)
                }
            context.startService(intent)
        }
    }
}
