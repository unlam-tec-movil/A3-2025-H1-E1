package ar.edu.unlam.scaffoldingandroid3.data.di

import ar.edu.unlam.scaffoldingandroid3.data.repository.RouteRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.domain.repository.RouteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para la inyección de dependencias de los repositorios.
 *
 * Este módulo es parte de la capa de datos y define cómo se deben
 * proporcionar las implementaciones concretas de los repositorios.
 * Utiliza @Binds para indicar a Hilt qué implementación usar para
 * cada interfaz de repositorio.
 *
 * @InstallIn(SingletonComponent::class) indica que las dependencias
 * proporcionadas por este módulo tendrán un único ciclo de vida
 * durante toda la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    /**
     * Proporciona la implementación concreta de RouteRepository.
     *
     * @param routeRepositoryImpl La implementación concreta a usar
     * @return La interfaz RouteRepository
     */
    @Binds
    @Singleton
    abstract fun bindRouteRepository(routeRepositoryImpl: RouteRepositoryImpl): RouteRepository
}
