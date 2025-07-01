package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.SensorRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.TrackingSessionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import org.junit.Assert.assertTrue

class StartTrackingUseCaseTest {

    @Test
    fun `si no hay permisos de ubicacion, retorna error`() = runBlocking {
        // Given
        val trackingSessionRepository = mockk<TrackingSessionRepository>(relaxed = true)
        val locationRepository = mockk<LocationRepository>(relaxed = true)
        val sensorRepository = mockk<SensorRepository>(relaxed = true)

        coEvery { locationRepository.hasLocationPermissions() } returns false

        val useCase = StartTrackingUseCase(
            trackingSessionRepository,
            locationRepository,
            sensorRepository
        )

        // When
        val result = useCase.execute("Ruta de prueba")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Permisos de ubicación no otorgados") == true)
    }
}