package ar.edu.unlam.scaffoldingandroid3.ui.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview

/**
 * Composable que muestra un indicador de carga centrado en la pantalla.
 */
@Composable
fun LoadingSpinner() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.testTag("loading_spinner_indicator"))
    }
}

@Preview
@Composable
fun LoadingSpinnerPreview() {
    LoadingSpinner()
}
