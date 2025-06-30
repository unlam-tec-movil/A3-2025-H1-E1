package ar.edu.unlam.scaffoldingandroid3.data.repository

import android.content.Context
import ar.edu.unlam.scaffoldingandroid3.data.local.dao.LocationPointDao
import ar.edu.unlam.scaffoldingandroid3.data.local.dao.TrackingDao
import ar.edu.unlam.scaffoldingandroid3.data.local.mapper.LocationPointEntityMapper.toDomainList
import ar.edu.unlam.scaffoldingandroid3.data.local.mapper.LocationPointEntityMapper.toEntityList
import ar.edu.unlam.scaffoldingandroid3.data.local.mapper.TrackingSessionEntityMapper.toEntity
import ar.edu.unlam.scaffoldingandroid3.data.sensor.MetricsCalculator
import ar.edu.unlam.scaffoldingandroid3.data.sensor.TrackingService
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingStatus
import ar.edu.unlam.scaffoldingandroid3.domain.repository.TrackingSessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del TrackingSessionRepository - Adaptador para gestión de sesiones
 * Conecta el dominio con el TrackingService y MetricsCalculator
 * Respeta Clean Architecture: Domain → Data → Android Services
 */
@Singleton
class TrackingSessionRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val metricsCalculator: MetricsCalculator,
        private val trackingDao: TrackingDao,
        private val locationPointDao: LocationPointDao,
    ) : TrackingSessionRepository {
        private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        private var timerJob: Job? = null

        private val currentSession = MutableStateFlow<TrackingSession?>(null)
        private val trackingStatus = MutableStateFlow(TrackingStatus.COMPLETED)
        private val currentMetrics =
            MutableStateFlow(
                TrackingMetrics(
                    currentSpeed = 0.0,
                    averageSpeed = 0.0,
                    maxSpeed = 0.0,
                    currentDistance = 0.0,
                    currentDuration = 0L,
                    currentElevation = 0.0,
                    totalSteps = 0,
                    lastLocation = null,
                ),
            )

        /**
         * Inicia una nueva sesión de tracking
         * Compatible con CU-010: Comenzar grabación de ruta
         */
        override suspend fun startTrackingSession(routeName: String): TrackingSession {
            val currentTime = System.currentTimeMillis()

            val session =
                TrackingSession(
                    routeName = routeName,
                    startTime = currentTime,
                    endTime = 0L,
                    metrics = metricsCalculator.getCurrentMetrics(),
                    status = TrackingStatus.ACTIVE,
                    routePoint = emptyList(),
                    photo = ""
                )

            currentSession.value = session
            trackingStatus.value = TrackingStatus.ACTIVE

            // Iniciar el servicio de tracking
            TrackingService.startService(context)

            // Iniciar timer para actualizar métricas cada segundo
            startMetricsTimer()

            return session
        }

        override fun setPhoto(uri: String) {
            currentSession.value = currentSession.value?.copy(photo = uri)
        }

        /**
         * Pausa la sesión de tracking actual
         * Compatible con CU-011: Pausar grabación de ruta
         */
        override suspend fun pauseTrackingSession(): TrackingSession? {
            val session = currentSession.value ?: return null

            val pausedSession =
                session.copy(
                    status = TrackingStatus.PAUSED,
                    metrics = metricsCalculator.getCurrentMetrics(),
                )

            currentSession.value = pausedSession
            trackingStatus.value = TrackingStatus.PAUSED

            // Pausar el servicio
            TrackingService.pauseService(context)

            // Pausar timer
            stopMetricsTimer()

            return pausedSession
        }

        /**
         * Reanuda la sesión de tracking pausada
         * Compatible con CU-012: Reanudar grabación pausada
         */
        override suspend fun resumeTrackingSession(): TrackingSession? {
            val session = currentSession.value ?: return null

            if (session.status != TrackingStatus.PAUSED) return null

            val resumedSession =
                session.copy(
                    status = TrackingStatus.ACTIVE,
                )

            currentSession.value = resumedSession
            trackingStatus.value = TrackingStatus.ACTIVE

            // Reanudar el servicio
            TrackingService.resumeService(context)

            // Reanudar timer
            startMetricsTimer()

            return resumedSession
        }

        /**
         * Finaliza la sesión de tracking actual
         * Compatible con CU-014: Finalizar grabación de ruta
         */
        override suspend fun stopTrackingSession(): TrackingSession? {
            val session = currentSession.value ?: return null

            val finalSession =
                session.copy(
                    endTime = System.currentTimeMillis(),
                    status = TrackingStatus.COMPLETED,
                    metrics = metricsCalculator.getCurrentMetrics(),
                    routePoint = metricsCalculator.getAllRoutePoints(),
                )

            // Mantener sesión completada disponible para SaveRoute
            currentSession.value = finalSession
            trackingStatus.value = TrackingStatus.COMPLETED

            // Detener el servicio
            TrackingService.stopService(context)

            // Detener timer
            stopMetricsTimer()

            return finalSession
        }

        /**
         * Obtiene la sesión de tracking actual
         */
        override suspend fun getCurrentTrackingSession(): TrackingSession? {
            return currentSession.value
        }

        /**
         * Obtiene el estado actual del tracking
         */
        override fun getTrackingStatus(): Flow<TrackingStatus> {
            return trackingStatus.asStateFlow()
        }

        /**
         * Obtiene las métricas en tiempo real
         * Compatible con CU-013: Ver estadísticas durante grabación
         */
        override fun getCurrentMetrics(): Flow<TrackingMetrics> {
            return currentMetrics.asStateFlow()
        }

        /**
         * Guarda una sesión de tracking completa en base de datos
         */
        override suspend fun saveTrackingSession(session: TrackingSession): Long {
            return try {
                // Usar mapper para Clean Architecture
                val sessionEntity = session.toEntity()

                // Guardar sesión en BD y obtener ID
                val sessionId = trackingDao.insertTrackingSession(sessionEntity)

                // Guardar puntos GPS de la ruta (usando mapper)
                if (session.routePoint.isNotEmpty()) {
                    val locationPointEntities = session.routePoint.toEntityList(sessionId)
                    locationPointDao.insertLocationPoints(locationPointEntities)
                }

                // TODO: Guardar fotos asociadas si las hay
                // Las fotos se guardarían aquí usando sessionId

                sessionId
            } catch (e: Exception) {
                throw Exception("Error al guardar sesión de tracking: ${e.message}")
            }
        }

        /**
         * Formatea tiempo en milisegundos a HH:MM:SS
         */
        private fun formatTime(millis: Long): String {
            val seconds = (millis / 1000) % 60
            val minutes = (millis / (1000 * 60)) % 60
            val hours = millis / (1000 * 60 * 60)
            return "%02d:%02d:%02d".format(hours, minutes, seconds)
        }

        /**
         * Verifica si hay una sesión activa
         */
        override suspend fun hasActiveSession(): Boolean {
            val session = currentSession.value
            return session != null && session.status != TrackingStatus.COMPLETED
        }

        /**
         * Obtiene el tiempo transcurrido de la sesión actual
         */
        override suspend fun getElapsedTime(): Long {
            val session = currentSession.value ?: return 0L

            return when (session.status) {
                TrackingStatus.ACTIVE -> System.currentTimeMillis() - session.startTime
                TrackingStatus.PAUSED -> session.endTime - session.startTime
                TrackingStatus.COMPLETED -> session.endTime - session.startTime
            }
        }

        /**
         * Actualiza las métricas en tiempo real (llamado desde servicio)
         */
        override fun updateMetrics(metrics: TrackingMetrics) {
            currentMetrics.value = metrics

            // Actualizar sesión actual con nuevas métricas
            currentSession.value?.let { session ->
                currentSession.value = session.copy(metrics = metrics)
            }
        }

        /**
         * Inicia el timer para actualizar métricas cada segundo
         */
        private fun startMetricsTimer() {
            stopMetricsTimer() // Cancelar timer anterior si existe

            timerJob =
                repositoryScope.launch {
                    while (trackingStatus.value == TrackingStatus.ACTIVE) {
                        try {
                            // Solo emitir métricas del MetricsCalculator sin modificarlas
                            // El tiempo ya está siendo calculado correctamente en MetricsCalculator
                            val updatedMetrics = metricsCalculator.getCurrentMetrics()
                            currentMetrics.value = updatedMetrics

                            // Actualizar sesión con las métricas actuales (sin recalcular tiempo)
                            currentSession.value?.let { session ->
                                currentSession.value = session.copy(metrics = updatedMetrics)
                            }

                            delay(1000L) // Actualizar cada segundo
                        } catch (e: Exception) {
                            // Manejar errores silenciosamente
                            break
                        }
                    }
                }
        }

        /**
         * Detiene el timer de métricas
         */
        private fun stopMetricsTimer() {
            timerJob?.cancel()
            timerJob = null
        }

        /**
         * Obtiene los puntos de la ruta en tiempo real para dibujar en el mapa
         */
        override suspend fun getCurrentRoutePoints(): List<ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint> {
            return metricsCalculator.getCurrentRoutePoints()
        }

        /**
         * Limpia la sesión completada después de guardar
         */
        override suspend fun clearCompletedSession() {
            currentSession.value = null
        }

        /**
         * Obtiene los puntos GPS de una sesión guardada
         */
        override suspend fun getLocationPointsBySession(
            sessionId: Long,
        ): List<ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint> {
            return try {
                val locationPointEntities = locationPointDao.getLocationPointsBySession(sessionId)
                locationPointEntities.toDomainList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        /**
         * Cleanup cuando se destruye el repositorio
         */
        fun cleanup() {
            repositoryScope.cancel()
        }
    }
