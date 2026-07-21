package com.example.internet_explorer.app.feature.cleanup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.internet_explorer.app.data.repository.CleanupResult
import com.example.internet_explorer.app.data.repository.GameStateRepository
import com.example.internet_explorer.app.data.repository.TraceLogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CleanupUiState(
    val logEntries: List<TraceLogEntry> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val currentTrace: Float = 0f,
    val result: CleanupResult? = null
)

class CleanupViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CleanupUiState())
    val uiState: StateFlow<CleanupUiState> = _uiState.asStateFlow()

    init {
        // traceLog và traceLevel đến từ Repository (nguồn sự thật), selectedIds/result
        // là state cục bộ của riêng màn hình này -- combine để giữ cả 2 phần cùng lúc
        // mà không ghi đè lựa chọn người chơi đang chọn dở.
        viewModelScope.launch {
            combine(
                GameStateRepository.traceLog,
                GameStateRepository.traceLevel
            ) { log, trace -> log to trace }
                .collect { (log, trace) ->
                    _uiState.update { it.copy(logEntries = log, currentTrace = trace) }
                }
        }
    }

    fun toggleEntry(entryId: String) {
        _uiState.update { state ->
            val newSelected = if (entryId in state.selectedIds) {
                state.selectedIds - entryId
            } else {
                state.selectedIds + entryId
            }
            state.copy(selectedIds = newSelected, result = null)
        }
    }

    fun submit() {
        val result = GameStateRepository.submitCleanup(_uiState.value.selectedIds)
        _uiState.update { it.copy(result = result, selectedIds = emptySet()) }
    }
}