package ar.edu.unlam.scaffoldingandroid3.ui.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.ui.shared.ErrorDialog
import ar.edu.unlam.scaffoldingandroid3.ui.shared.LoadingSpinner

/**
 * Pantalla de historial de actividades
 * Usa el viewModel que nos traiga la info de la base de datos
 * y la convierta a domain para poder pasarsela a la lista cuando
 * eso se implemente
 */
@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isLoading -> {
                LoadingSpinner()
            }
            uiState.error != null -> {
                ErrorDialog(
                    errorMessage = uiState.error!!,
                    onDismiss = { viewModel.clearError() },
                )
            }
            uiState.isEmpty -> {
                Text(
                    text = stringResource(id = R.string.history_empty_message),
                    modifier = Modifier.padding(16.dp),
                )
            }
            else -> {
                HistoryList(
                    historyList = uiState.historyList,
                    onDeleteItem = { historyId ->
                        viewModel.deleteHistoryItem(historyId)
                    },
                )
            }
        }
    }
}
