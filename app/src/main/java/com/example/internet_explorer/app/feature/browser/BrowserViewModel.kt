package com.example.internet_explorer.app.feature.browser

import androidx.lifecycle.ViewModel
import com.example.internet_explorer.app.data.local.WebsiteContent
import com.example.internet_explorer.app.data.local.WebsiteJson
import com.example.internet_explorer.app.data.local.parseContent
import com.example.internet_explorer.app.data.repository.GameStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface BrowserUiState {
    data object Loading : BrowserUiState
    data class Success(val website: WebsiteJson, val content: WebsiteContent) : BrowserUiState
    data object NotFound : BrowserUiState
}

class BrowserViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<BrowserUiState>(BrowserUiState.Loading)
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    fun openWebsite(entityId: String) {
        _uiState.value = BrowserUiState.Loading
        val website = GameStateRepository.getWebsite(entityId)
        if (website == null) {
            _uiState.value = BrowserUiState.NotFound
            return
        }
        // Tương đương recordVisitUseCase ở mục 13.2.3 GDD
        GameStateRepository.markWebsiteVisited(entityId)
        val content = website.parseContent(GameStateRepository.getGson())
        _uiState.value = BrowserUiState.Success(website, content)
    }
}