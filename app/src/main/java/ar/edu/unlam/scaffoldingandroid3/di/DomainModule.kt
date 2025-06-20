package ar.edu.unlam.scaffoldingandroid3.di

import ar.edu.unlam.scaffoldingandroid3.domain.logic.RouteDistanceCalculator
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
} 