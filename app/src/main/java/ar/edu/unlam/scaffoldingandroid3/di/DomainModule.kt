package ar.edu.unlam.scaffoldingandroid3.di

import ar.edu.unlam.scaffoldingandroid3.domain.logic.RouteDistanceCalculator
import ar.edu.unlam.scaffoldingandroid3.domain.logic.RouteDisplayCalculator
import ar.edu.unlam.scaffoldingandroid3.domain.repository.RouteRepository
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.SaveApiRouteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    @Singleton
    fun provideRouteDistanceCalculator(): RouteDistanceCalculator {
        return RouteDistanceCalculator()
    }
    
    @Provides
    @Singleton
    fun provideRouteDisplayCalculator(): RouteDisplayCalculator {
        return RouteDisplayCalculator()
    }
    
    @Provides
    @Singleton
    fun provideSaveApiRouteUseCase(
        routeRepository: RouteRepository
    ): SaveApiRouteUseCase {
        return SaveApiRouteUseCase(routeRepository)
    }
}
