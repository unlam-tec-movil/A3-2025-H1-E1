package ar.edu.unlam.scaffoldingandroid3.ui.tracking

/**
 * Estados de la pantalla de tracking
 */
enum class TrackingScreenState {
    PREPARATION, // Preparación: input nombre, validaciones, botón start
    RECORDING, // Grabación: mapa activo, métricas básicas, pause/stop
    EXPANDED_STATS, // Estadísticas: panel expandido con todos los sensores
}

/**
 * Estado de UI para la pantalla de tracking
 */
data class TrackingUiState(
    val screenState: TrackingScreenState = TrackingScreenState.PREPARATION,
    val routeName: String = "",
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val isLoading: Boolean = false,
    val isStatsExpanded: Boolean = false,
    val canPause: Boolean = false,
    val canResume: Boolean = false,
    val canStop: Boolean = false,
    val error: String? = null,
)
