package ar.edu.unlam.scaffoldingandroid3.ui.shared

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class LoadingSpinnerTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingSpinner_isDisplayed_whenComposableIsRendered() {
        // Arrange
        composeTestRule.setContent {
            LoadingSpinner()
        }

        // Act & Assert
        composeTestRule.onNodeWithTag("loading_spinner_indicator").assertIsDisplayed()
    }
}
