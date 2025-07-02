package ar.edu.unlam.scaffoldingandroid3.domain.logic

import android.location.Location
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RouteDistanceCalculatorTest {
    private val calculator = RouteDistanceCalculator()

    @Before
    fun setup() {
        mockkStatic(Location::class)
    }

    @Test
    fun `calculate deberia retornar cero para una lista vacia de puntos`() {
        // Given
        val points = emptyList<Route.Point>()

        // When
        val distance = calculator.calculate(points)

        // Then
        assertEquals(0.0, distance, 0.1)
    }

    @Test
    fun `calculate deberia retornar cero para un solo punto`() {
        // Given
        val points =
            listOf(
                Route.Point(
                    latitude = -34.670533,
                    longitude = -58.562867,
                    timestamp = 0L,
                ),
            )

        // When
        val distance = calculator.calculate(points)

        // Then
        assertEquals(0.0, distance, 0.1)
    }

    @Test
    fun `calculate deberia retornar la distancia correcta entre dos puntos`() {
        // Given
        val points =
            listOf(
                Route.Point(
                    latitude = -34.670533,
                    longitude = -58.562867,
                    timestamp = 0L,
                ),
                Route.Point(
                    latitude = -34.671533,
                    longitude = -58.563867,
                    timestamp = 1000L,
                ),
            )

        // Mock del metodo estatico distanceBetween
        every {
            Location.distanceBetween(
                -34.670533,
                -58.562867,
                -34.671533,
                -58.563867,
                any(),
            )
        } answers {
            val results = arg<FloatArray>(4)
            results[0] = 150f
        }

        // When
        val distance = calculator.calculate(points)

        // Then
        assertEquals(150.0, distance, 0.1)
    }

    @Test
    fun `calculate deberia sumar todas las distancias para multiples puntos`() {
        // Given
        val points =
            listOf(
                Route.Point(
                    latitude = -34.670533,
                    longitude = -58.562867,
                    timestamp = 0L,
                ),
                Route.Point(
                    latitude = -34.671533,
                    longitude = -58.563867,
                    timestamp = 1000L,
                ),
                Route.Point(
                    latitude = -34.672533,
                    longitude = -58.564867,
                    timestamp = 2000L,
                ),
            )

        // Mock para el primer segmento
        every {
            Location.distanceBetween(
                -34.670533,
                -58.562867,
                -34.671533,
                -58.563867,
                any(),
            )
        } answers {
            val results = arg<FloatArray>(4)
            results[0] = 100f
        }

        // Mock para el segundo segmento
        every {
            Location.distanceBetween(
                -34.671533,
                -58.563867,
                -34.672533,
                -58.564867,
                any(),
            )
        } answers {
            val results = arg<FloatArray>(4)
            results[0] = 150f
        }

        // When
        val distance = calculator.calculate(points)

        // Then
        assertEquals(250.0, distance, 0.1)
    }
}
