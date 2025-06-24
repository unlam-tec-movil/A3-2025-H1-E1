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
import kotlin.math.sqrt

/**
 * Gestor de sensores del dispositivo SIMPLIFICADO
 * Solo funcionalidad básica para tracking
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
        
        init {
            // Log sensor availability for debugging
            Log.d("DeviceSensorManager", "Sensor availability check:")
            Log.d("DeviceSensorManager", "- Accelerometer: ${accelerometer != null}")
            Log.d("DeviceSensorManager", "- Barometer: ${barometer != null}")
        }

        // Variables simples
        private var stepCount = 0
        
        // Variables para pause/resume
        private var isPaused = false
        private var stepsAtPause = 0
        
        // Variables para detección mejorada de pasos
        private var lastStepTime = 0L
        private var lastMagnitude = 0.0f
        private var magnitudeHistory = mutableListOf<Float>()
        private val magnitudeWindowSize = 10 // Ventana de 10 samples para filtrado

        /**
         * Conteo de pasos básico desde acelerómetro
         */
        fun getStepUpdates(): Flow<Int> =
            callbackFlow {
                if (accelerometer == null) {
                    // Acelerómetro no disponible - funcionalidad degradada gracefully
                    Log.w("DeviceSensorManager", "Accelerometer not available - step tracking disabled")
                    trySend(0) // Pasos por defecto
                    close() // Cerrar flow sin error
                    return@callbackFlow
                }

                val stepListener =
                    object : SensorEventListener {
                        override fun onSensorChanged(event: SensorEvent?) {
                            event?.let {
                                if (it.sensor.type == Sensor.TYPE_ACCELEROMETER && !isPaused) {
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

                // Usar SENSOR_DELAY_GAME para mejor precisión en detección de pasos
                // NORMAL=200ms, GAME=20ms - mejor para detectar picos de pasos
                sensorManager.registerListener(
                    stepListener,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_GAME,
                )

                awaitClose {
                    sensorManager.unregisterListener(stepListener)
                }
            }.distinctUntilChanged()

        /**
         * Altitud básica desde barómetro
         */
        fun getAltitudeUpdates(): Flow<Double> =
            callbackFlow {
                if (barometer == null) {
                    // Barómetro no disponible - funcionalidad degradada gracefully
                    Log.w("DeviceSensorManager", "Barometer not available - altitude tracking will use GPS fallback")
                    trySend(0.0) // Altitud por defecto a nivel del mar
                    close() // Cerrar flow sin error
                    return@callbackFlow
                }

                val barometerListener =
                    object : SensorEventListener {
                        override fun onSensorChanged(event: SensorEvent?) {
                            event?.let {
                                if (it.sensor.type == Sensor.TYPE_PRESSURE) {
                                    val pressure = it.values[0]
                                    val altitude = calculateSimpleAltitude(pressure)
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
            }

        /**
         * Detección CONSERVADORA de pasos - Enfoque simple y confiable
         * Estrategia: "Es mejor contar de menos que de más"
         * - Detecta solo picos pronunciados y espaciados en el tiempo
         * - Prefiere precisión sobre sensibilidad
         */
        private fun detectStep(accelerometerValues: FloatArray): Int {
            val x = accelerometerValues[0]
            val y = accelerometerValues[1] 
            val z = accelerometerValues[2]
            val currentTime = System.currentTimeMillis()

            // Cálculo de magnitud
            val magnitude = sqrt(x * x + y * y + z * z)
            
            // Agregar a historial de magnitudes para suavizado
            magnitudeHistory.add(magnitude)
            if (magnitudeHistory.size > magnitudeWindowSize) {
                magnitudeHistory.removeAt(0)
            }
            
            // Calcular promedio móvil para filtrar ruido
            val avgMagnitude = if (magnitudeHistory.size >= 3) {
                magnitudeHistory.takeLast(3).average().toFloat()
            } else {
                magnitude
            }
            
            // CRITERIOS CONSERVADORES - "Mejor contar de menos que de más"
            val minTimeBetweenSteps = 500L // Mínimo 500ms entre pasos (120 pasos/min máximo)
            val stepThreshold = 14.5f      // Umbral conservador
            val peakThreshold = 2.5f       // Picos más pronunciados para evitar ruido
            
            // 1. Verificar tiempo mínimo entre pasos
            if (currentTime - lastStepTime < minTimeBetweenSteps) {
                lastMagnitude = avgMagnitude
                return 0
            }
            
            // 2. Detectar PICO: magnitud actual > umbral Y mayor que anterior
            val isPeak = avgMagnitude > stepThreshold && 
                        avgMagnitude > (lastMagnitude + peakThreshold)
            
            // 3. Verificar que no es ruido (magnitud no excesiva)
            val isReasonableMagnitude = avgMagnitude < 25.0f // Evitar sacudidas violentas
            
            lastMagnitude = avgMagnitude
            
            if (isPeak && isReasonableMagnitude) {
                lastStepTime = currentTime
                Log.d("StepDetection", "Step detected: magnitude=$avgMagnitude (conservative algorithm)")
                return 1
            }
            
            return 0
        }

        /**
         * Cálculo simple de altitud
         */
        private fun calculateSimpleAltitude(pressure: Float): Double {
            // Fórmula simple: cada hPa de diferencia ≈ 8.3m de altitud
            val seaLevelPressure = 1013.25f
            return ((seaLevelPressure - pressure) * 8.3).toDouble()
        }

        /**
         * Resetea el conteo de pasos y variables de detección
         */
        fun resetStepCount() {
            stepCount = 0
            lastStepTime = 0L
            lastMagnitude = 0.0f
            magnitudeHistory.clear()
        }

        /**
         * Funciones de pausa/resume
         */
        fun pauseStepTracking() {
            isPaused = true
            stepsAtPause = stepCount
        }

        fun resumeStepTracking() {
            isPaused = false
            // Mantener el conteo desde donde se pausó
        }

        /**
         * Verificaciones de disponibilidad
         */
        fun isAccelerometerAvailable() = accelerometer != null

        fun isBarometerAvailable() = barometer != null

        fun getCurrentStepCount() = stepCount
    }
