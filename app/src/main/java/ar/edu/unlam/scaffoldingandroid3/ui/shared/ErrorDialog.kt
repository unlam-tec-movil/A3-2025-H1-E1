package ar.edu.unlam.scaffoldingandroid3.ui.shared

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Composable que muestra un diálogo de error con un mensaje y un botón para cerrarlo.
 *
 * @param errorMessage el mensaje de error a mostrar.
 * @param onDismiss la acción a ejecutar cuando el diálogo se cierra.
 */
@Composable
fun ErrorDialog(errorMessage: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Error") },
        text = { Text(text = errorMessage) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
