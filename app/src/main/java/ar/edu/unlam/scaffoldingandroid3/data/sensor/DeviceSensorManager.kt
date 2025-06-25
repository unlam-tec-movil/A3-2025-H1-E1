package ar.edu.unlam.scaffoldingandroid3.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de sensores del dispositivo.
 * Utiliza TYPE_STEP_COUNTER para un conteo de pasos preciso.
 */
@Singleton
class DeviceSensorManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // --- SENSORES CORRECTOS ---
        private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        private val barometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        // --- ESTADO INTERNO PARA EL CONTEO DE PASOS ---
        private var initialSteps = -1L // Usamos -1 para saber que aún no hemos capturado el valor inicial
        private var sessionSteps = 0 // Pasos calculados para la sesión actual

        init {
            Log.d("DeviceSensorManager", "Sensor availability check:")
            Log.d("DeviceSensorManager", "- Step Counter: ${stepCounterSensor != null}")
            Log.d("DeviceSensorManager", "- Barometer: ${barometer != null}")
            if (stepCounterSensor == null) {
                Log.w("DeviceSensorManager", "CRITICAL: Step Counter sensor not available. Step tracking will be disabled.")
            }
        }

        /**
         * Emite el conteo de pasos para la sesión actual usando el sensor TYPE_STEP_COUNTER.
         */
        fun getStepUpdates(): Flow<Int> =
            callbackFlow {
                if (stepCounterSensor == null) {
                    Log.w("DeviceSensorManager", "Step Counter not available, closing flow.")
                    trySend(0)
                    close()
                    return@callbackFlow
                }

                val stepListener =
                    object : SensorEventListener {
                        override fun onSensorChanged(event: SensorEvent?) {
                            event?.let {
                                if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                                    val totalStepsSinceBoot = it.values[0].toLong()
                                    // Raw sensor value: $totalStepsSinceBoot

                                    // 1. Si es la primera lectura de la sesión, la guardamos como punto de partida.
                                    if (initialSteps == -1L) {
                                        initialSteps = totalStepsSinceBoot
                                        Log.d("StepCounterLogic", "Initial steps for session set to: $initialSteps")
                                    }

                                    // 2. Los pasos de la sesión son la diferencia entre el total actual y el inicial.
                                    sessionSteps = (totalStepsSinceBoot - initialSteps).toInt()
                                    // Session steps: $sessionSteps

                                    trySend(sessionSteps)
                                }
                            }
                        }

                        override fun onAccuracyChanged(
                            sensor: Sensor?,
                            accuracy: Int,
                        ) {}
                    }

                val registered =
                    sensorManager.registerListener(
                        stepListener,
                        stepCounterSensor,
                        SensorManager.SENSOR_DELAY_NORMAL,
                    )

                if (!registered) {
                    Log.e("DeviceSensorManager", "CRITICAL: Failed to register step counter listener")
                    close(Exception("Failed to register step counter"))
                } else {
                    Log.d("DeviceSensorManager", "Step Counter listener registered.")
                }

                awaitClose {
                    Log.d("DeviceSensorManager", "Unregistering step counter listener")
                    sensorManager.unregisterListener(stepListener)
                }
            }.distinctUntilChanged()

        /**
         * Altitud básica desde barómetro (sin cambios, ya estaba bien)
         */
        fun getAltitudeUpdates(): Flow<Double> =
            callbackFlow {
                if (barometer == null) {
                    trySend(0.0)
                    close()
                    return@callbackFlow
                }
                val barometerListener =
                    object : SensorEventListener {
                        override fun onSensorChanged(event: SensorEvent?) {
                            event?.values?.firstOrNull()?.let { pressure ->
                                val altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure)
                                trySend(altitude.toDouble())
                            }
                        }

                        override fun onAccuracyChanged(
                            sensor: Sensor?,
                            accuracy: Int,
                        ) {}
                    }
                sensorManager.registerListener(barometerListener, barometer, SensorManager.SENSOR_DELAY_NORMAL)
                awaitClose { sensorManager.unregisterListener(barometerListener) }
            }

        /**
         * Resetea las variables de conteo para una nueva sesión de tracking.
         * Esta función es CRUCIAL y es llamada por el TrackingService.
         */
        fun resetStepCount() {
            initialSteps = -1L
            sessionSteps = 0
            Log.d("StepCounterLogic", "Step count variables have been reset for a new session.")
        }

        // Los métodos de pausa/reanudación ya no son necesarios para el conteo de pasos con este enfoque
        fun pauseStepTracking() {}

        fun resumeStepTracking() {}

        fun isAccelerometerAvailable() = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null

        fun isStepCounterAvailable() = stepCounterSensor != null

        fun isBarometerAvailable() = barometer != null

        fun getCurrentStepCount(): Int = sessionSteps
    }
