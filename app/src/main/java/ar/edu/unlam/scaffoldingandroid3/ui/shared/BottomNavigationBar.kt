package ar.edu.unlam.scaffoldingandroid3.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import ar.edu.unlam.scaffoldingandroid3.navigation.Screen

/**
 * Barra de navegación inferior que permite moverse entre las diferentes secciones de la app.
 *
 * Este componente es parte de la capa de UI y proporciona:
 * - Navegación entre pantallas principales
 * - Indicador visual de la pantalla actual
 * - Acceso rápido a funcionalidades principales
 *
 * @param modifier Modificador para personalizar el layout del componente
 */

@Composable
fun BottomNavigationBar(
    navController: NavHostController, modifier: Modifier = Modifier
) {
    // Observa la ruta actualmente activa
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = modifier.height(80.dp),
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Ítem: Guardados
            NavigationItem(
                icon = Icons.Filled.Favorite,
                label = "Guardados",
                route = Screen.MyRoutes.route,
                currentRoute = currentRoute,
                navController = navController
            )
            // Ítem: Mapa (pantalla de inicio)
            NavigationItem(
                icon = Icons.Filled.Place,
                label = "Mapa",
                route = Screen.Map.route,
                currentRoute = currentRoute,
                navController = navController
            )
            // Ítem: Historial
            NavigationItem(
                icon = Icons.AutoMirrored.Filled.List,
                label = "Historial",
                route = Screen.History.route,
                currentRoute = currentRoute,
                navController = navController
            )
        }
    }
}

/**
 * Representa cada ítem de la BottomBar.
 *
 * Cambia color de fondo y tint según si está seleccionado.
 */
@Composable
private fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    route: String,
    currentRoute: String?,
    navController: NavHostController
) {
    // Define estado de selección
    val selected = currentRoute == route

    // Mantiene la estética: mismo tamaño y forma
    val containerColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.primaryContainer

    // Tint dinámico: ícono claro sobre fondo primario, o primario normal
    val iconTint = if (selected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor)
                .clickable {
                    // Navega evitando múltiples instancias y restaurando estado
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            // 1) Pop hasta la pantalla de inicio del grafo (MapScreen)
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                                inclusive = false //Guarda el estado del mapa
                            }
                            // 2) Evita apilar varias instancias
                            launchSingleTop = true
                            // 3) No restaura el estado guardado
                            /*
                            Tener en cuenta que "restoreState = false" hace que se elimine el estado
                            de la pantalla superpuesta, esto hace que se pierda la instancia
                            de TrackingScreen()
                            TODO: Investigar funcionamiento en casos de uso al grabar ruta
                            */
                            restoreState = false
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = iconTint
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}