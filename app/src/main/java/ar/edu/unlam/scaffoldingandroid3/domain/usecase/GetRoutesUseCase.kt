package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.domain.repository.RouteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener todas las rutas almacenadas.
 *
 * Este caso de uso es parte de la capa de dominio y encapsula la lógica
 * de negocio para obtener las rutas. Sigue el patrón Use Case para
 * mantener la lógica de negocio aislada y reutilizable.
 *
 * @property repository El repositorio de rutas inyectado por Hilt
 */
class GetRoutesUseCase
    @Inject
    constructor(
        private val repository: RouteRepository,
    ) {
        /**
         * Ejecuta el caso de uso.
         *
         * @return Un Flow que emite la lista de rutas
         */
        operator fun invoke(): Flow<List<Route>> = repository.getAllRoutes()
    }

/**
 * TODO: Caso de uso
 *
 * ⚠️ CUIDADO: Podría ser "PASS-THROUGH" (PASA-MANOS)
 * Si solo llama repository.[método]() sin lógica de negocio,
 * usar Repository directo en ViewModel.
 *
 * SOLUCIÓN: ViewModel usa Repository directo para operaciones simples.
 */
