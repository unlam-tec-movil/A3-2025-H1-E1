package ar.edu.unlam.scaffoldingandroid3.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * TODO: ViewModel - Lógica del historial de actividades
 */

@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        private val historyRepository: HistoryRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HistoryUiState())
        val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

        init {
            loadHistory()
        }

        fun loadHistory() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                try {
                    val historyList = historyRepository.getHistory()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            historyList = historyList,
                            isEmpty = historyList.isEmpty(),
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Error al cargar el historial",
                        )
                    }
                }
            }
        }

        fun deleteHistoryItem(historyId: Long) {
            viewModelScope.launch {
                try {
                    historyRepository.deleteHistory(historyId)
                    loadHistory()
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(error = e.message ?: "Error al eliminar el elemento")
                    }
                }
            }
        }

        fun clearError() {
            _uiState.update { it.copy(error = null) }
        }
    }
