package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.SensorRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.TrackingSessionRepository
import javax.inject.Inject

/**
 * Caso de uso - Iniciar sesión de tracking GPS
 * Valida permisos, GPS activo y crea nueva sesión de grabación de ruta
 * Compatible con CU-010: Comenzar grabación de ruta
 *
 * Respeta Clean Architecture: Use Case → Repository → Service
 */
class StartTrackingUseCase
    @Inject
    constructor(
        private val trackingSessionRepository: TrackingSessionRepository,
        private val locationRepository: LocationRepository,
        private val sensorRepository: SensorRepository,
    ) {
        /**
         * Inicia una nueva sesión de tracking
         * @param routeName Nombre de la ruta a grabar
         * @return Result con la sesión creada o error
         */
        suspend fun execute(routeName: String): Result<TrackingSession> {
            return try {
                // 1. Verificar permisos de ubicación
                if (!locationRepository.hasLocationPermissions()) {
                    return Result.failure(Exception("Permisos de ubicación no otorgados"))
                }

                // 2. Verificar si ya hay una sesión activa
                if (trackingSessionRepository.hasActiveSession()) {
                    return Result.failure(Exception("Ya hay una sesión de tracking activa"))
                }

                // 3. Verificar disponibilidad de sensores básicos
                if (!sensorRepository.isAccelerometerAvailable()) {
                    return Result.failure(Exception("Acelerómetro no disponible"))
                }

                // 4. Resetear contadores
                sensorRepository.resetStepCount()

                // 5. Iniciar tracking de ubicación
                val locationStarted = locationRepository.startLocationTracking()
                if (!locationStarted) {
                    return Result.failure(Exception("No se pudo iniciar el tracking GPS"))
                }

                // 6. Iniciar tracking de sensores
                sensorRepository.startSensorTracking()

                // 7. Crear nueva sesión de tracking
                val session = trackingSessionRepository.startTrackingSession(routeName)

                Result.success(session)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        /**
         * Verifica si se puede iniciar tracking
         */
        suspend fun canStartTracking(): Boolean {
            return locationRepository.hasLocationPermissions() &&
                !trackingSessionRepository.hasActiveSession() &&
                sensorRepository.isAccelerometerAvailable()
        }

        /**
         * Obtiene los motivos por los que no se puede iniciar tracking
         */
        suspend fun getBlockingReasons(): List<String> {
            val reasons = mutableListOf<String>()

            if (!locationRepository.hasLocationPermissions()) {
                reasons.add("Permisos de ubicación no otorgados")
            }

            if (trackingSessionRepository.hasActiveSession()) {
                reasons.add("Ya hay una sesión activa")
            }

            if (!sensorRepository.isAccelerometerAvailable()) {
                reasons.add("Acelerómetro no disponible")
            }

            return reasons
        }
    }
