package ar.edu.unlam.scaffoldingandroid3.data.di

import ar.edu.unlam.scaffoldingandroid3.data.repository.HistoryRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.data.repository.RouteRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.domain.repository.HistoryRepository
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

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(historyRepositoryImpl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: ar.edu.unlam.scaffoldingandroid3.data.repository.LocationRepositoryImpl,
    ): ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository

    @Binds
    @Singleton
    abstract fun bindTrackingSessionRepository(
        trackingSessionRepositoryImpl: ar.edu.unlam.scaffoldingandroid3.data.repository.TrackingSessionRepositoryImpl,
    ): ar.edu.unlam.scaffoldingandroid3.domain.repository.TrackingSessionRepository

    @Binds
    @Singleton
    abstract fun bindSensorRepository(
        sensorRepositoryImpl: ar.edu.unlam.scaffoldingandroid3.data.repository.SensorRepositoryImpl,
    ): ar.edu.unlam.scaffoldingandroid3.domain.repository.SensorRepository

    @Binds
    @Singleton
    abstract fun bindCameraRepository(
        cameraRepositoryImpl: ar.edu.unlam.scaffoldingandroid3.data.repository.CameraRepositoryImpl,
    ): ar.edu.unlam.scaffoldingandroid3.domain.repository.CameraRepository
}
