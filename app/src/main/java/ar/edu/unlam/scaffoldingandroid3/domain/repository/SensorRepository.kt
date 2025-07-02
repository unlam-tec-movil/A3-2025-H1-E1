package ar.edu.unlam.scaffoldingandroid3.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Puerto de salida - Sensores del dispositivo SIMPLIFICADO
 * Solo acelerómetro y barómetro para tracking básico
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
     * Verifica disponibilidad de sensores
     */
    fun isAccelerometerAvailable(): Boolean

    fun isBarometerAvailable(): Boolean
}
