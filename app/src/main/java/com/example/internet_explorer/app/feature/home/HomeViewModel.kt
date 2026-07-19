package com.example.internet_explorer.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.internet_explorer.app.data.local.WebsiteJson
import com.example.internet_explorer.app.data.repository.GameStateRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class WebsiteListItem(
    val website: WebsiteJson,
    val visited: Boolean
)

data class HomeUiState(
    val items: List<WebsiteListItem> = emptyList()
)

class HomeViewModel : ViewModel() {

    val uiState: StateFlow<HomeUiState> = GameStateRepository.progress
        .map { progress ->
            val items = GameStateRepository.getAllWebsites().map { site ->
                WebsiteListItem(website = site, visited = site.id in progress.visitedWebsiteIds)
            }
            HomeUiState(items = items)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}