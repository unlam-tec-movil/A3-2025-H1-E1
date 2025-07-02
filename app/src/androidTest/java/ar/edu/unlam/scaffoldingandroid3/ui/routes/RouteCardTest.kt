package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import org.junit.Rule
import org.junit.Test

class RouteCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun routeCardMuestraInformacionDeLaRuta() {
        // Given
        val testRoute =
            Route(
                id = "test-route",
                name = "Ruta de Prueba",
                distance = 5000.0, // ID sin UUID/prefijo = API_ORIGINAL = metros
                duration = 3600000, // 1 hora en milisegundos
                points = emptyList(),
                photoUri = "",
            )

        // When
        composeTestRule.setContent {
            RouteCard(
                route = testRoute,
                onPlayClick = { },
                onDeleteItem = { },
            )
        }

        // Then
        composeTestRule.onNodeWithText("Ruta de Prueba").assertIsDisplayed()
    }

    @Test
    fun routeCardEjecutaCallbackAlHacerClickEnPlay() {
        // Given
        var playClicked = false
        val testRoute =
            Route(
                id = "test-route",
                name = "Test Route",
                distance = 1000.0, // ID sin UUID/prefijo = API_ORIGINAL = metros
                duration = 600000, // 10 minutos en milisegundos
                points = emptyList(),
                photoUri = "",
            )

        // When
        composeTestRule.setContent {
            RouteCard(
                route = testRoute,
                onPlayClick = { playClicked = true },
                onDeleteItem = { },
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Play").performClick()
        assert(playClicked)
    }
}
