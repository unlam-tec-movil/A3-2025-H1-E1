package ar.edu.unlam.scaffoldingandroid3.data.di

import android.content.Context
import android.hardware.SensorManager
import ar.edu.unlam.scaffoldingandroid3.data.remote.OverpassApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private const val OVERPASS_API_URL = "https://overpass-api.de/api/"

/**
 * Módulo Hilt para servicios Android - Sensores y Location Services
 * Integración con Clean Architecture - capa de Data
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context,
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideSensorManager(
        @ApplicationContext context: Context,
    ): SensorManager {
        return context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(OVERPASS_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOverpassApi(retrofit: Retrofit): OverpassApi {
        return retrofit.create(OverpassApi::class.java)
    }
}
