package ar.edu.unlam.scaffoldingandroid3.domain.repository

import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define las operaciones de persistencia para las rutas.
 *
 * Esta interfaz es parte de la capa de dominio y define el contrato que deben
 * implementar los repositorios concretos. Sigue el patrón Repository para
 * abstraer las operaciones de persistencia.
 *
 * Las implementaciones concretas pueden usar Room, Retrofit u otras fuentes de datos
 * sin que el dominio necesite conocer estos detalles.
 */
interface RouteRepository {
    /**
     * Obtiene las rutas cercanas desde una fuente de datos remota (API).
     *
     * @param latitude Latitud de la ubicación actual
     * @param longitude Longitud de la ubicación actual
     * @param radius Radio de búsqueda en metros
     * @return Un Result con la lista de rutas cercanas o un error.
     */
    suspend fun getNearbyRoutes(latitude: Double, longitude: Double, radius: Int): Result<List<Route>>

    /**
     * Guarda una ruta en el repositorio.
     *
     * @param route La ruta a guardar
     */
    suspend fun saveRoute(route: Route)

    /**
     * Obtiene una ruta específica por su ID.
     *
     * @param id El identificador de la ruta
     * @return La ruta encontrada o null si no existe
     */
    suspend fun getRoute(id: String): Route?

    /**
     * Obtiene todas las rutas almacenadas.
     *
     * @return Un Flow que emite la lista de rutas
     */
    fun getAllRoutes(): Flow<List<Route>>

    /**
     * Elimina una ruta específica.
     *
     * @param id El identificador de la ruta a eliminar
     */
    suspend fun deleteRoute(id: String)
}
