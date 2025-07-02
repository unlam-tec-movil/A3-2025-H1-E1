package ar.edu.unlam.scaffoldingandroid3

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import ar.edu.unlam.scaffoldingandroid3.ui.navigation.NavGraph
import ar.edu.unlam.scaffoldingandroid3.ui.shared.BottomNavigationBar
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
@RequiresApi(Build.VERSION_CODES.Q)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScaffoldingAndroid3Theme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavigationBar(navController = navController) },
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
