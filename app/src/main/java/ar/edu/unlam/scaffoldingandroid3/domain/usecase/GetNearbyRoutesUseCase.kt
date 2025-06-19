package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.domain.repository.RouteRepository
import javax.inject.Inject

/**
 * Caso de uso para obtener rutas cercanas a una ubicación.
 *
 * Este caso de uso encapsula la lógica para obtener rutas desde el repositorio
 * basándose en coordenadas geográficas.
 */
class GetNearbyRoutesUseCase @Inject constructor(
    private val repository: RouteRepository
) {
    suspend operator fun invoke(latitude: Double, longitude: Double, radius: Int): Result<List<Route>> {
        return repository.getNearbyRoutes(latitude, longitude, radius)
    }
}
