package ar.edu.unlam.scaffoldingandroid3.data.repository

import android.content.Context
import ar.edu.unlam.scaffoldingandroid3.data.sensor.MetricsCalculator
import ar.edu.unlam.scaffoldingandroid3.data.sensor.TrackingService
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingStatus
import ar.edu.unlam.scaffoldingandroid3.domain.repository.TrackingSessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    ) : TrackingSessionRepository {
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
                    totalElevationGain = 0.0,
                    totalElevationLoss = 0.0,
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
                )

            currentSession.value = session
            trackingStatus.value = TrackingStatus.ACTIVE

            // Iniciar el servicio de tracking
            TrackingService.startService(context)

            return session
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

            currentSession.value = null
            trackingStatus.value = TrackingStatus.COMPLETED

            // Detener el servicio
            TrackingService.stopService(context)

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
         * Guarda una sesión de tracking completa
         */
        override suspend fun saveTrackingSession(session: TrackingSession): Long {
            // TODO: Implementar guardado en base de datos cuando esté configurada
            // Por ahora retornamos un ID simulado
            return System.currentTimeMillis()
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
        fun updateMetrics(metrics: TrackingMetrics) {
            currentMetrics.value = metrics

            // Actualizar sesión actual con nuevas métricas
            currentSession.value?.let { session ->
                currentSession.value = session.copy(metrics = metrics)
            }
        }
    }
