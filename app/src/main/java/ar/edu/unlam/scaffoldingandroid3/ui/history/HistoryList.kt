package ar.edu.unlam.scaffoldingandroid3.ui.history

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import ar.edu.unlam.scaffoldingandroid3.domain.model.History

/**
 * Lista de historial de actividades
 * Recibe por parámetro un data class History que tenga toda la info para pasarle al HistoryCard
 * y un onDeleteClick dinámico, para que el composable no sea el que decida a donde va,
 * sino que sea la screen cuando lo llame el que decida que info va y a donde redirige.
 */
@Composable
fun HistoryList(
    historyList: List<History>,
    onDeleteItem: (Long) -> Unit
) {
    LazyColumn {
        items(historyList) { history ->
            HistoryCard(
                history = history,
                onDeleteClick = { onDeleteItem(history.id) }
            )
        }
    }
}
