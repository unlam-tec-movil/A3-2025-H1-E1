package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * TODO
 * Usar el viewModel que nos traiga la info de la base de datos
 * y la convierta a domain para poder pasarsela a la lista cuando
 * eso se implemente
 */
@Composable
fun MyRoutesScreen() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Text(
            text = "Mis rutas",
            style = MaterialTheme.typography.titleLarge,
        )
        RouteList(
            userName = "userName",
            location = "location",
            distance = "distance",
            duration = "duration",
        )
    }
}
