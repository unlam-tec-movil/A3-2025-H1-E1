package ar.edu.unlam.scaffoldingandroid3.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Puerto de salida - Sensores del dispositivo
 * Define métodos para acceso a acelerómetro, barómetro y magnetómetro
 * Compatible con CU-010 a CU-014: conteo de pasos, altitud y brújula
 */
interface SensorRepository {
    /**
     * Obtiene actualizaciones del conteo de pasos
     */
    fun getStepUpdates(): Flow<Int>

    /**
     * Obtiene actualizaciones de altitud del barómetro
     */
    fun getAltitudeUpdates(): Flow<Double>

    /**
     * Obtiene actualizaciones de orientación del magnetómetro
     */
    fun getCompassUpdates(): Flow<Float>

    /**
     * Inicia el tracking de sensores
     */
    suspend fun startSensorTracking()

    /**
     * Detiene el tracking de sensores
     */
    suspend fun stopSensorTracking()

    /**
     * Pausa el tracking de sensores
     */
    suspend fun pauseSensorTracking()

    /**
     * Reanuda el tracking de sensores
     */
    suspend fun resumeSensorTracking()

    /**
     * Resetea el conteo de pasos
     */
    suspend fun resetStepCount()

    /**
     * Obtiene el conteo actual de pasos
     */
    suspend fun getCurrentStepCount(): Int

    /**
     * Obtiene la altitud actual
     */
    suspend fun getCurrentAltitude(): Double

    /**
     * Obtiene la orientación actual (azimuth)
     */
    suspend fun getCurrentAzimuth(): Float

    /**
     * Verifica disponibilidad de sensores
     */
    fun isAccelerometerAvailable(): Boolean

    fun isBarometerAvailable(): Boolean

    fun isMagnetometerAvailable(): Boolean

    /**
     * Verifica si está actualmente caminando
     */
    fun isCurrentlyWalking(): Boolean
}
