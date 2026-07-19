package com.example.internet_explorer.app.feature.puzzle

import androidx.lifecycle.ViewModel
import com.example.internet_explorer.app.data.local.LogLineJson
import com.example.internet_explorer.app.data.repository.GameStateRepository
import com.example.internet_explorer.app.data.repository.PuzzleSubmitResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PuzzleUiState(
    val title: String = "",
    val logLines: List<LogLineJson> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val hintText: String = "",
    val result: PuzzleSubmitResult? = null
)

class PuzzleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PuzzleUiState())
    val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

    init {
        val puzzle = GameStateRepository.getPuzzle()
        _uiState.value = PuzzleUiState(
            title = puzzle.title,
            logLines = puzzle.logLines,
            hintText = puzzle.hintText
        )
    }

    fun toggleLine(lineId: String) {
        _uiState.update { state ->
            val newSelected = if (lineId in state.selectedIds) {
                state.selectedIds - lineId
            } else {
                state.selectedIds + lineId
            }
            // Reset kết quả cũ khi người chơi đổi lựa chọn, tránh hiển thị sai
            state.copy(selectedIds = newSelected, result = null)
        }
    }

    fun submit() {
        val result = GameStateRepository.submitPuzzleAnswer(_uiState.value.selectedIds)
        _uiState.update { it.copy(result = result) }
    }
}