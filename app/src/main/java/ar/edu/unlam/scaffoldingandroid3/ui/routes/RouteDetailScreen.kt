package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * TODO: Composable - Pantalla de detalle completo de ruta
 * UI: RouteDetailCard + botones favorito/compartir + información de ruta
 * Navegación: recibe routeId, botón "Iniciar" navega a TrackingScreen
 */

@Composable
fun RouteDetailScreen(
    routeId: String,
    onStartClick: () -> Unit
) {
    Text("Pantalla de Detalle de Ruta")
}
