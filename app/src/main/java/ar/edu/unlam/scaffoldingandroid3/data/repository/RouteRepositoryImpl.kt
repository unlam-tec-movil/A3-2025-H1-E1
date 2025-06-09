package ar.edu.unlam.scaffoldingandroid3.data.repository

import ar.edu.unlam.scaffoldingandroid3.data.local.dao.RouteDao
import ar.edu.unlam.scaffoldingandroid3.data.local.mapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.data.local.mapper.toEntity
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.domain.repository.RouteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

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
class RouteRepositoryImpl
    @Inject
    constructor(
        private val dao: RouteDao
    ) : RouteRepository {
        override suspend fun saveRoute(route: Route) {
            val entity = route.toEntity()
            dao.insert(entity)
        }

        override suspend fun getRoute(id: String): Route? {
            val entity = dao.getRoute(id)?: return null
            return entity.toDomain()
        }

        override fun getAllRoutes(): Flow<List<Route>> =
            dao.getAllRoutes().map { list -> list.map { it.toDomain() } }

        override suspend fun deleteRoute(id: String) {
            dao.deleteRoute(id)
        }
    }
