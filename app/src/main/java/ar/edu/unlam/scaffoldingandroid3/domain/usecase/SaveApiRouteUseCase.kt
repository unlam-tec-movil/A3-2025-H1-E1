package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.domain.repository.RouteRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

/**
 * Use Case para guardar rutas provenientes de la API
 * Maneja la lógica de negocio para persistir rutas externas
 */
class SaveApiRouteUseCase
    @Inject
    constructor(
        private val routeRepository: RouteRepository,
    ) {
        /**
         * Guarda una ruta de la API en el almacenamiento local
         * Genera un ID apropiado que preserve el origen
         */
        suspend fun execute(apiRoute: Route): Result<Unit> {
            return try {
                // Crear una nueva ruta con ID que identifique su origen API
                val localRoute =
                    apiRoute.copy(
                        id = generateApiRouteId(),
                    )

                routeRepository.saveRoute(localRoute)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        /**
         * Verifica si una ruta de API ya está guardada
         * Usa tolerancia de distancia para evitar duplicados
         */
        suspend fun isAlreadySaved(route: Route): Boolean {
            return try {
                val savedRoutes = routeRepository.getAllRoutes().first()
                savedRoutes.any { savedRoute ->
                    savedRoute.name == route.name &&
                        kotlin.math.abs(savedRoute.distance - route.distance) < DISTANCE_TOLERANCE_METERS
                }
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Genera un ID único para rutas guardadas desde API
         * El prefijo "api-" permite identificar el origen
         */
        private fun generateApiRouteId(): String {
            return "api-${UUID.randomUUID()}"
        }

        companion object {
            private const val DISTANCE_TOLERANCE_METERS = 10.0
        }
    }
