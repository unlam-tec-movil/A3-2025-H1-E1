package ar.edu.unlam.scaffoldingandroid3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ar.edu.unlam.scaffoldingandroid3.ui.screens.MapScreen
import ar.edu.unlam.scaffoldingandroid3.ui.theme.ScaffoldingAndroid3Theme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Actividad principal de la aplicación.
 * 
 * Esta actividad es el punto de entrada de la UI y configura:
 * - El tema de la aplicación
 * - La navegación principal
 * - El scaffold base
 * 
 * Utiliza @AndroidEntryPoint para permitir la inyección de dependencias
 * de Hilt en la actividad y sus componentes.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScaffoldingAndroid3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapScreen(
                        onNewRouteClick = {
                            // TODO: Implementar navegación a la pantalla de nueva ruta
                        },
                        onLoadRoutesClick = {
                            // TODO: Implementar navegación a la pantalla de cargar rutas
                        }
                    )
                }
            }
        }
    }
}
