package ar.edu.unlam.scaffoldingandroid3.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.unlam.scaffoldingandroid3.ui.components.BottomNavigationBar
import ar.edu.unlam.scaffoldingandroid3.ui.components.RouteCard

@Preview(showBackground = true)
@Composable
fun MyRoutesScreen(){
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Mis rutas",
                style = MaterialTheme.typography.titleLarge
            )

            RouteCard()

        }
        BottomNavigationBar(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
        )
    }
}