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
                distance = 5000.0, // 5000 metros = 5.0 km
                duration = 3600000, // 60 minutos
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
        composeTestRule.onNodeWithText("5.0 km").assertIsDisplayed()
        composeTestRule.onNodeWithText("60 min").assertIsDisplayed()
    }

    @Test
    fun routeCardEjecutaCallbackAlHacerClickEnPlay() {
        // Given
        var playClicked = false
        val testRoute =
            Route(
                id = "test-route",
                name = "Test Route",
                distance = 1000.0, // 1000 metros = 1.0 km
                duration = 600000, // 10 minutos
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
