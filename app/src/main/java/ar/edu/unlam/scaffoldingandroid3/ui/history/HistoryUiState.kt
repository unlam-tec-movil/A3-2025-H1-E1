package ar.edu.unlam.scaffoldingandroid3.ui.history

import ar.edu.unlam.scaffoldingandroid3.domain.model.History

/**
 * Data class - Estado UI del historial
 */

data class HistoryUiState(
    val isLoading: Boolean = false,
    val historyList: List<History> = emptyList(),
    val error: String? = null,
    val isEmpty: Boolean = false,
)
