package ar.edu.unlam.scaffoldingandroid3.data.di

import android.content.Context
import androidx.room.Room
import ar.edu.unlam.scaffoldingandroid3.data.local.AppDatabase
import ar.edu.unlam.scaffoldingandroid3.data.local.dao.RouteDao
import ar.edu.unlam.scaffoldingandroid3.data.repository.RouteRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.domain.repository.RouteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

/**
 * Módulo Hilt - Configuración Room database
 * @Provides @Singleton AppDatabase, todos los DAOs
 * Room.databaseBuilder + configuración de database
 */

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(appContext: Context): AppDatabase =
        Room.databaseBuilder(appContext, AppDatabase::class.java, "routes.db")
            .build()

    @Provides
    fun provideRouteDao(db: AppDatabase): RouteDao = db.routeDao()

    @Provides
    @Singleton
    fun provideRouteRepository(dao: RouteDao): RouteRepository = RouteRepositoryImpl(dao)
}
