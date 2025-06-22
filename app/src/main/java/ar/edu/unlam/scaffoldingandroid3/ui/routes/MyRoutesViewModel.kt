package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.GetRoutesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class MyRoutesViewModel
@Inject constructor(
    private val getRoutesUseCase: GetRoutesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyRoutesUiState())
    val uiState: StateFlow<MyRoutesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getRoutesUseCase().onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }.collect { list ->
                    if (list.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = null,
                                emptyMessage = "No hay rutas guardadas"
                            )
                        }
                        return@collect
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false, savedRoutes = list, error = null, emptyMessage = null
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, emptyMessage = null) }
    }
}
