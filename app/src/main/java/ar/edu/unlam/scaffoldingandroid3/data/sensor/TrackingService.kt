package ar.edu.unlam.scaffoldingandroid3.data.sensor

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ar.edu.unlam.scaffoldingandroid3.MainActivity
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
import kotlinx.coroutines.flow.collect
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

    private val binder = TrackingBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Estados del servicio
    private val _trackingStatus = MutableStateFlow(TrackingStatus.COMPLETED)
    val trackingStatus: StateFlow<TrackingStatus> = _trackingStatus.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    private val _altitude = MutableStateFlow(0.0)
    val altitude: StateFlow<Double> = _altitude.asStateFlow()

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

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

        // Verificar permiso POST_NOTIFICATIONS para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Sin permiso, iniciar como servicio normal
                return
            }
        }
        
        startForeground(NOTIFICATION_ID, createNotification())

        // Resetear contadores
        sensorManager.resetStepCount()
        metricsCalculator.reset()

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
        stopSelf()
    }

    private fun startLocationTracking() {
        locationJob =
            serviceScope.launch {
                locationService.getLocationUpdates()
                    .catch { e ->
                        // Manejar errores de GPS
                    }
                    .collect { location ->
                        _currentLocation.value = location
                        metricsCalculator.addLocationPoint(location)
                    }
            }
    }

    private fun startSensorTracking() {
        sensorJobs =
            listOf(
                // Tracking de pasos
                serviceScope.launch {
                    sensorManager.getStepUpdates()
                        .collect { steps ->
                            _stepCount.value = steps
                            metricsCalculator.updateStepCount(steps)
                        }
                },
                // Tracking de altitud
                serviceScope.launch {
                    sensorManager.getAltitudeUpdates()
                        .collect { altitude ->
                            _altitude.value = altitude
                            metricsCalculator.updateAltitude(altitude)
                        }
                },
                // Tracking de brújula
                serviceScope.launch {
                    sensorManager.getCompassUpdates()
                        .collect { azimuth ->
                            _azimuth.value = azimuth
                        }
                },
            )
    }

    /**
     * Obtiene el tiempo transcurrido excluyendo pausas
     */
    fun getElapsedTime(): Long {
        return when (_trackingStatus.value) {
            TrackingStatus.ACTIVE -> System.currentTimeMillis() - startTime - pausedDuration
            TrackingStatus.PAUSED -> pauseStartTime - startTime - pausedDuration
            TrackingStatus.COMPLETED -> 0L
        }
    }

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
        val intent =
            Intent(this, MainActivity::class.java)
                .apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    setPackage(packageName)
                }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE,
            )

        val statusText =
            when (_trackingStatus.value) {
                TrackingStatus.ACTIVE -> "Grabando ruta..."
                TrackingStatus.PAUSED -> "Grabación pausada"
                TrackingStatus.COMPLETED -> "Finalizado"
            }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Senderismo - Tracking Activo")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(createPauseResumeAction())
            .addAction(createStopAction())
            .build()
    }

    private fun createPauseResumeAction(): NotificationCompat.Action {
        val actionText = if (_trackingStatus.value == TrackingStatus.ACTIVE) "Pausar" else "Reanudar"
        val action = if (_trackingStatus.value == TrackingStatus.ACTIVE) ACTION_PAUSE_TRACKING else ACTION_RESUME_TRACKING

        val intent =
            Intent(this, TrackingService::class.java)
                .apply {
                    this.action = action
                    setPackage(packageName)
                }
        val pendingIntent =
            PendingIntent.getService(
                this,
                1,
                intent,
                PendingIntent.FLAG_IMMUTABLE,
            )

        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_media_pause,
            actionText,
            pendingIntent,
        ).build()
    }

    private fun createStopAction(): NotificationCompat.Action {
        val intent =
            Intent(this, TrackingService::class.java)
                .apply {
                    action = ACTION_STOP_TRACKING
                    setPackage(packageName)
                }
        val pendingIntent =
            PendingIntent.getService(
                this,
                2,
                intent,
                PendingIntent.FLAG_IMMUTABLE,
            )

        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Finalizar",
            pendingIntent,
        ).build()
    }

    private fun updateNotification() {
        // Verificar permiso POST_NOTIFICATIONS para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
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
                }
            context.startService(intent)
        }

        fun resumeService(context: Context) {
            val intent =
                Intent(context, TrackingService::class.java).apply {
                    action = ACTION_RESUME_TRACKING
                }
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent =
                Intent(context, TrackingService::class.java).apply {
                    action = ACTION_STOP_TRACKING
                }
            context.startService(intent)
        }
    }
}
