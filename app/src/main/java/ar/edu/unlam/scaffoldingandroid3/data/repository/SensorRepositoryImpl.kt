package ar.edu.unlam.scaffoldingandroid3.data.repository

import ar.edu.unlam.scaffoldingandroid3.data.sensor.DeviceSensorManager
import ar.edu.unlam.scaffoldingandroid3.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del SensorRepository - Adaptador para sensores del dispositivo
 * Conecta el dominio con los servicios de sensores Android (DeviceSensorManager)
 * Respeta Clean Architecture: Domain → Data → Android Services
 */
@Singleton
class SensorRepositoryImpl
    @Inject
    constructor(
        private val sensorManager: DeviceSensorManager,
    ) : SensorRepository {
        /**
         * Obtiene actualizaciones del conteo de pasos
         */
        override fun getStepUpdates(): Flow<Int> {
            return sensorManager.getStepUpdates()
        }

        /**
         * Obtiene actualizaciones de altitud del barómetro
         */
        override fun getAltitudeUpdates(): Flow<Double> {
            return sensorManager.getAltitudeUpdates()
        }

        /**
         * Obtiene actualizaciones de orientación del magnetómetro
         */
        override fun getCompassUpdates(): Flow<Float> {
            return sensorManager.getCompassUpdates()
        }

        /**
         * Inicia el tracking de sensores
         */
        override suspend fun startSensorTracking() {
            // Los sensores se inician automáticamente cuando se suscriben a los flows
            // El DeviceSensorManager maneja el ciclo de vida de los sensores
        }

        /**
         * Detiene el tracking de sensores
         */
        override suspend fun stopSensorTracking() {
            // Los sensores se detienen cuando se cancela la suscripción a los flows
        }

        /**
         * Pausa el tracking de sensores
         */
        override suspend fun pauseSensorTracking() {
            sensorManager.pauseStepTracking()
        }

        /**
         * Reanuda el tracking de sensores
         */
        override suspend fun resumeSensorTracking() {
            sensorManager.resumeStepTracking()
        }

        /**
         * Resetea el conteo de pasos
         */
        override suspend fun resetStepCount() {
            sensorManager.resetStepCount()
        }

        /**
         * Obtiene el conteo actual de pasos
         */
        override suspend fun getCurrentStepCount(): Int {
            return sensorManager.getCurrentStepCount()
        }

        /**
         * Obtiene la altitud actual
         */
        override suspend fun getCurrentAltitude(): Double {
            return sensorManager.currentPressure.toDouble() // Sería mejor calcular la altitud real
        }

        /**
         * Obtiene la orientación actual (azimuth)
         */
        override suspend fun getCurrentAzimuth(): Float {
            return sensorManager.getCurrentAzimuth()
        }

        /**
         * Verifica disponibilidad de sensores
         */
        override fun isAccelerometerAvailable(): Boolean {
            return sensorManager.isAccelerometerAvailable()
        }

        override fun isBarometerAvailable(): Boolean {
            return sensorManager.isBarometerAvailable()
        }

        override fun isMagnetometerAvailable(): Boolean {
            return sensorManager.isMagnetometerAvailable()
        }

        /**
         * Verifica si está actualmente caminando
         */
        override fun isCurrentlyWalking(): Boolean {
            return sensorManager.isCurrentlyWalking()
        }
    }
