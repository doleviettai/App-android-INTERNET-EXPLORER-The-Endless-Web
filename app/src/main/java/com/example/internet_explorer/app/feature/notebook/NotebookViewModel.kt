package com.example.internet_explorer.app.feature.notebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.internet_explorer.app.data.local.ClueJson
import com.example.internet_explorer.app.data.local.WorldFactJson
import com.example.internet_explorer.app.data.repository.GameStateRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class NotebookUiState(
    val collectedFacts: List<WorldFactJson> = emptyList(),
    val unlockedClues: List<ClueJson> = emptyList(),
    val progressPercent: Int = 0
)

class NotebookViewModel : ViewModel() {

    // Notebook completion (mục 14 GDD): % manh mối thu thập, thay cho "level".
    val uiState: StateFlow<NotebookUiState> = GameStateRepository.progress
        .map {
            val facts = GameStateRepository.getNotebookFacts()
            val clues = GameStateRepository.getNotebookClues()
            val total = GameStateRepository.getTotalFactCount()
            NotebookUiState(
                collectedFacts = facts,
                unlockedClues = clues,
                progressPercent = if (total > 0) facts.size * 100 / total else 0
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotebookUiState())
}