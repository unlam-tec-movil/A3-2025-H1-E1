package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ar.edu.unlam.scaffoldingandroid3.ui.shared.BottomNavigationBar

/**
 * TODO
 * Usar el viewModel que nos traiga la info de la base de datos
 * y la convierta a domain para poder pasarsela a la lista cuando
 * eso se implemente
 */
@Composable
fun MyRoutesScreen(
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(modifier = Modifier.fillMaxWidth())
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
}
