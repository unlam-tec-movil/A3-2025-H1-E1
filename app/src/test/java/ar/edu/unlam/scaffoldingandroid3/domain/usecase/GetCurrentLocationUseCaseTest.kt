package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationPoint
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCurrentLocationUseCaseTest {

    @Test
    fun `when repository returns a location, use case should return the same location`() = runBlocking {
        // Given
        val mockRepository = mockk<LocationRepository>()
        val useCase = GetCurrentLocationUseCase(mockRepository)
        val expectedLocation = LocationPoint(
            latitude = -34.0,
            longitude = -58.0,
            altitude = 10.0,
            speed = 0.0f,
            accuracy = 5.0f,
            timestamp = System.currentTimeMillis()
        )

        coEvery { mockRepository.getLastKnownLocation() } returns expectedLocation

        // When
        val result = useCase()

        // Then
        assertEquals(expectedLocation, result)
    }
} 