package ar.edu.unlam.scaffoldingandroid3.ui.shared

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ErrorDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun errorDialogMuestraElMensajeDeError() {
        val mensaje = "Ocurrió un error inesperado"
        composeTestRule.setContent {
            ErrorDialog(errorMessage = mensaje, onDismiss = {})
        }
        composeTestRule.onNodeWithText(mensaje).assertExists()
    }
}