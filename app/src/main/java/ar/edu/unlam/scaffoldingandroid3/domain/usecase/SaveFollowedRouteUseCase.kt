package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.History
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession
import ar.edu.unlam.scaffoldingandroid3.domain.repository.HistoryRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Caso de uso para guardar automáticamente en historial cuando se completa
 * el seguimiento de una ruta existente de la API (no una nueva ruta creada)
 */
class SaveFollowedRouteUseCase
    @Inject
    constructor(
        private val historyRepository: HistoryRepository,
    ) {
        /**
         * Guarda en historial el resultado de seguir una ruta existente
         * @param session Sesión completada de tracking
         * @param originalRouteName Nombre de la ruta original que se siguió
         */
        suspend fun execute(
            session: TrackingSession,
            originalRouteName: String,
        ): Result<Unit> {
            return try {
                val history = buildHistoryFromSession(session, originalRouteName)
                historyRepository.saveCompletedActivity(history)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        private fun buildHistoryFromSession(
            session: TrackingSession,
            originalRouteName: String,
        ): History {
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            return History(
                routeName = "Siguiendo: $originalRouteName",
                date = dateStr,
                metrics = session.metrics,
                photoUri = session.photo,
                routePoint = session.routePoint,
            )
        }
    }
