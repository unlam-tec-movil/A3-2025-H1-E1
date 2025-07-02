package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingMetrics
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingSession
import ar.edu.unlam.scaffoldingandroid3.domain.model.TrackingStatus
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.SensorRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.TrackingSessionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StopTrackingUseCaseTest {
    @Test
    fun `should stop tracking successfully when active session exists`() =
        runBlocking {
            // Given
            val mockTrackingRepository = mockk<TrackingSessionRepository>()
            val mockLocationRepository = mockk<LocationRepository>()
            val mockSensorRepository = mockk<SensorRepository>()
            val useCase =
                StopTrackingUseCase(
                    trackingSessionRepository = mockTrackingRepository,
                    locationRepository = mockLocationRepository,
                    sensorRepository = mockSensorRepository,
                )

            val testSession =
                TrackingSession(
                    id = 1L,
                    routeName = "Test Route",
                    startTime = System.currentTimeMillis(),
                    endTime = System.currentTimeMillis(),
                    metrics = TrackingMetrics(),
                    status = TrackingStatus.COMPLETED,
                    routePoint = emptyList(),
                    photo = "",
                )

            coEvery { mockTrackingRepository.getCurrentTrackingSession() } returns testSession
            coEvery { mockLocationRepository.stopLocationTracking() } returns Unit
            coEvery { mockSensorRepository.stopSensorTracking() } returns Unit
            coEvery { mockTrackingRepository.stopTrackingSession() } returns testSession

            // When
            val result = useCase.execute()

            // Then
            assertTrue(result.isSuccess)
            assertEquals(testSession, result.getOrNull())
        }

    @Test
    fun `should return error when no active session exists`() =
        runBlocking {
            // Given
            val mockTrackingRepository = mockk<TrackingSessionRepository>()
            val mockLocationRepository = mockk<LocationRepository>()
            val mockSensorRepository = mockk<SensorRepository>()
            val useCase =
                StopTrackingUseCase(
                    trackingSessionRepository = mockTrackingRepository,
                    locationRepository = mockLocationRepository,
                    sensorRepository = mockSensorRepository,
                )

            coEvery { mockTrackingRepository.getCurrentTrackingSession() } returns null

            // When
            val result = useCase.execute()

            // Then
            assertTrue(result.isFailure)
            assertEquals("No hay sesión de tracking activa", result.exceptionOrNull()?.message)
        }
}
