package ar.edu.unlam.scaffoldingandroid3.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Gestor de sensores del dispositivo para tracking de senderismo
 * Maneja: Acelerómetro (pasos), Barómetro (altitud), Magnetómetro (brújula)
 */
@Singleton
class DeviceSensorManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Sensores del dispositivo
        private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        private val barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // Variables para conteo de pasos
        private var stepCount = 0
        private var lastStepTime = 0L
        private var previousMagnitude = 0f
        private var isWalking = false

        // Variables para altitud
        private var initialPressure: Float? = null
        var currentPressure = 0f
            private set

        // Variables para brújula
        private var azimuth = 0f

        /**
         * Obtiene datos del acelerómetro para conteo de pasos
         * Compatible con CU-010: "se habilita el conteo de pasos con el acelerómetro"
         */
        fun getStepUpdates(): Flow<Int> =
            callbackFlow {
                if (accelerometer == null) {
                    close(Exception("Acelerómetro no disponible"))
                    return@callbackFlow
                }

                val stepListener =
                    object : SensorEventListener {
                        override fun onSensorChanged(event: SensorEvent?) {
                            event?.let {
                                if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                                    val steps = detectStep(it.values)
                                    if (steps > 0) {
                                        stepCount += steps
                                        trySend(stepCount)
                                    }
                                }
                            }
                        }

                        override fun onAccuracyChanged(
                            sensor: Sensor?,
                            accuracy: Int,
                        ) {}
                    }

                sensorManager.registerListener(
                    stepListener,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_FASTEST,
                )

                awaitClose {
                    sensorManager.unregisterListener(stepListener)
                }
            }.distinctUntilChanged()

        /**
         * Obtiene datos del barómetro para cálculo de altitud
         * Compatible con CU-010: "se registra la altura actual y se calculan los desniveles"
         */
        fun getAltitudeUpdates(): Flow<Double> =
            callbackFlow {
                if (barometer == null) {
                    close(Exception("Barómetro no disponible"))
                    return@callbackFlow
                }

                val barometerListener =
                    object : SensorEventListener {
                        override fun onSensorChanged(event: SensorEvent?) {
                            event?.let {
                                if (it.sensor.type == Sensor.TYPE_PRESSURE) {
                                    currentPressure = it.values[0]

                                    // Establecer presión inicial para cálculo relativo
                                    if (initialPressure == null) {
                                        initialPressure = currentPressure
                                    }

                                    val altitude = calculateAltitudeFromPressure(currentPressure, initialPressure!!)
                                    trySend(altitude)
                                }
                            }
                        }

                        override fun onAccuracyChanged(
                            sensor: Sensor?,
                            accuracy: Int,
                        ) {}
                    }

                sensorManager.registerListener(
                    barometerListener,
                    barometer,
                    SensorManager.SENSOR_DELAY_NORMAL,
                )

                awaitClose {
                    sensorManager.unregisterListener(barometerListener)
                }
            }.distinctUntilChanged { old, new -> abs(old - new) < 0.5 } // Filtrar cambios menores a 0.5m

        /**
         * Obtiene datos del magnetómetro para brújula digital
         * Compatible con CU-003: brújula interactiva
         */
        fun getCompassUpdates(): Flow<Float> =
            callbackFlow {
                if (magnetometer == null) {
                    close(Exception("Magnetómetro no disponible"))
                    return@callbackFlow
                }

                val compassListener =
                    object : SensorEventListener {
                        override fun onSensorChanged(event: SensorEvent?) {
                            event?.let {
                                if (it.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                                    azimuth = calculateAzimuth(it.values)
                                    trySend(azimuth)
                                }
                            }
                        }

                        override fun onAccuracyChanged(
                            sensor: Sensor?,
                            accuracy: Int,
                        ) {}
                    }

                sensorManager.registerListener(
                    compassListener,
                    magnetometer,
                    SensorManager.SENSOR_DELAY_UI,
                )

                awaitClose {
                    sensorManager.unregisterListener(compassListener)
                }
            }.distinctUntilChanged { old, new -> abs(old - new) < 2f } // Filtrar cambios menores a 2 grados

        /**
         * Detecta pasos basado en datos del acelerómetro
         */
        private fun detectStep(accelerometerValues: FloatArray): Int {
            val x = accelerometerValues[0]
            val y = accelerometerValues[1]
            val z = accelerometerValues[2]

            // Calcular magnitud del vector aceleración
            val magnitude = sqrt(x * x + y * y + z * z)

            val currentTime = System.currentTimeMillis()

            // Detectar pico en la aceleración (paso)
            if (magnitude > STEP_THRESHOLD &&
                magnitude > previousMagnitude + STEP_SENSITIVITY &&
                currentTime - lastStepTime > MIN_STEP_INTERVAL
            ) {
                lastStepTime = currentTime
                previousMagnitude = magnitude
                isWalking = true
                return 1
            }

            // Actualizar magnitud anterior
            previousMagnitude = magnitude

            // Detectar si dejó de caminar
            if (currentTime - lastStepTime > WALKING_TIMEOUT) {
                isWalking = false
            }

            return 0
        }

        /**
         * Calcula altitud basada en presión barométrica
         */
        private fun calculateAltitudeFromPressure(
            currentPressure: Float,
            referencePressure: Float,
        ): Double {
            // Fórmula barométrica: h = 44330 * (1 - (P/P0)^(1/5.255))
            return 44330.0 * (1.0 - Math.pow((currentPressure / referencePressure).toDouble(), 1.0 / 5.255))
        }

        /**
         * Calcula azimuth (orientación) basado en magnetómetro
         */
        private fun calculateAzimuth(magneticValues: FloatArray): Float {
            // Simplificado - en implementación real se combinaría con acelerómetro
            val x = magneticValues[0]
            val y = magneticValues[1]

            var azimuth = Math.atan2(y.toDouble(), x.toDouble()) * 180 / Math.PI

            // Normalizar a 0-360 grados
            if (azimuth < 0) {
                azimuth += 360.0
            }

            return azimuth.toFloat()
        }

        /**
         * Resetea el conteo de pasos
         */
        fun resetStepCount() {
            stepCount = 0
        }

        /**
         * Pausa el tracking de pasos (para CU-011: pausar grabación)
         */
        fun pauseStepTracking() {
            isWalking = false
        }

        /**
         * Reanuda el tracking de pasos (para CU-012: reanudar grabación)
         */
        fun resumeStepTracking() {
            lastStepTime = System.currentTimeMillis()
        }

        /**
         * Verifica disponibilidad de sensores
         */
        fun isSensorAvailable(sensorType: Int): Boolean {
            return sensorManager.getDefaultSensor(sensorType) != null
        }

        fun isAccelerometerAvailable() = isSensorAvailable(Sensor.TYPE_ACCELEROMETER)

        fun isBarometerAvailable() = isSensorAvailable(Sensor.TYPE_PRESSURE)

        fun isMagnetometerAvailable() = isSensorAvailable(Sensor.TYPE_MAGNETIC_FIELD)

        fun getCurrentStepCount() = stepCount

        fun getCurrentAzimuth() = azimuth

        fun isCurrentlyWalking() = isWalking

        companion object {
            private const val STEP_THRESHOLD = 11.5f // Umbral para detectar paso
            private const val STEP_SENSITIVITY = 2.0f // Sensibilidad del detector
            private const val MIN_STEP_INTERVAL = 300L // Tiempo mínimo entre pasos (ms)
            private const val WALKING_TIMEOUT = 3000L // Tiempo para considerar que dejó de caminar
        }
    }
