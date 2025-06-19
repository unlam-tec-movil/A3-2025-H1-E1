package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Caso de uso para obtener la última ubicación conocida del usuario.
 *
 * Este caso de uso abstrae la lógica para obtener la ubicación desde el repositorio,
 * manteniendo el ViewModel agnóstico a la implementación de la capa de datos.
 */
class GetCurrentLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(): LocationPoint? {
        return locationRepository.getLastKnownLocation()
    }
} 