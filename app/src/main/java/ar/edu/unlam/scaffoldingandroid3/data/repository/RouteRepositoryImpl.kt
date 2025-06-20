package ar.edu.unlam.scaffoldingandroid3.data.repository

import ar.edu.unlam.scaffoldingandroid3.data.local.dao.RouteDao
import ar.edu.unlam.scaffoldingandroid3.data.local.mapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.data.local.mapper.toEntity
import ar.edu.unlam.scaffoldingandroid3.data.remote.OverpassApi
import ar.edu.unlam.scaffoldingandroid3.data.remote.mapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.domain.logic.RouteDistanceCalculator
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.domain.repository.RouteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import android.location.Location

/**
 * Implementación concreta del repositorio de rutas.
 *
 * Esta clase es parte de la capa de datos y proporciona la implementación
 * real de las operaciones definidas en RouteRepository. Actualmente es un
 * placeholder que devuelve datos vacíos, pero está preparada para integrarse
 * con Room para persistencia local.
 *
 * @property repository Las dependencias necesarias para la implementación
 *                     (Room, Retrofit, etc.) se inyectarán aquí
 */
class RouteRepositoryImpl @Inject constructor(
    private val dao: RouteDao,
    private val overpassApi: OverpassApi,
    private val distanceCalculator: RouteDistanceCalculator
) : RouteRepository {
    override suspend fun getNearbyRoutes(
        latitude: Double,
        longitude: Double,
        radius: Int,
        limit: Int?
    ): Result<List<Route>> {
        return try {
            val query =
                "[out:json];(relation[route=\"hiking\"](around:$radius,$latitude,$longitude););(._;>>;);out geom;"
            val response = overpassApi.getNearbyRoutes(query)
            val routes = response.toDomain(distanceCalculator)

            val resultRoutes = if (limit != null && routes.isNotEmpty()) {
                val centerLocation = Location("").apply {
                    this.latitude = latitude
                    this.longitude = longitude
                }
                routes.sortedBy { route ->
                    route.points.firstOrNull()?.let { startPoint ->
                        Location("").apply {
                            this.latitude = startPoint.latitude
                            this.longitude = startPoint.longitude
                        }.distanceTo(centerLocation)
                    } ?: Float.MAX_VALUE
                }.take(limit)
            } else {
                routes
            }

            Result.success(resultRoutes)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun saveRoute(route: Route) {
        val entity = route.toEntity()
        dao.insert(entity)
    }

    override suspend fun getRoute(id: String): Route? {
        val entity = dao.getRoute(id) ?: return null
        return entity.toDomain()
    }

    override fun getAllRoutes(): Flow<List<Route>> = dao.getAllRoutes().map { list -> list.map { it.toDomain() } }

    override suspend fun deleteRoute(id: String) {
        dao.deleteRoute(id)
    }
}
