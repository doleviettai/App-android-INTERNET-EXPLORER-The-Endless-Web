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
    data class Success(
        val website: WebsiteJson,
        val content: WebsiteContent,
        val isArchived: Boolean,
        val isUnstable: Boolean
    ) : BrowserUiState
    /** Site đã bị Site Decay xóa nội dung -- chỉ còn metadata, xem gameplay-mechanics-phase2.md mục 4. */
    data class Decayed(val website: WebsiteJson) : BrowserUiState
    data object NotFound : BrowserUiState
}

class BrowserViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<BrowserUiState>(BrowserUiState.Loading)
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private var currentEntityId: String? = null

    fun openWebsite(entityId: String) {
        currentEntityId = entityId
        _uiState.value = BrowserUiState.Loading
        val website = GameStateRepository.getWebsite(entityId)
        if (website == null) {
            _uiState.value = BrowserUiState.NotFound
            return
        }

        // Ghi lượt ghé TRƯỚC khi kiểm tra decay -- markWebsiteVisited tự quyết định có
        // cộng fact hay không dựa trên trạng thái decay tại đúng thời điểm ghé (xem patch
        // GameStateRepository.markWebsiteVisited bên dưới).
        GameStateRepository.markWebsiteVisited(entityId)

        if (GameStateRepository.isWebsiteDecayed(entityId)) {
            _uiState.value = BrowserUiState.Decayed(website)
            return
        }

        val content = website.parseContent(GameStateRepository.getGson())
        _uiState.value = BrowserUiState.Success(
            website = website,
            content = content,
            isArchived = GameStateRepository.isWebsiteArchived(entityId),
            isUnstable = GameStateRepository.isWebsiteUnstable(entityId)
        )
    }

    /** Gọi khi người chơi bấm "Lưu vào Notebook" -- cập nhật UI ngay, không cần mở lại trang. */
    fun archiveCurrentWebsite() {
        val entityId = currentEntityId ?: return
        GameStateRepository.archiveWebsite(entityId)
        val state = _uiState.value
        if (state is BrowserUiState.Success) {
            _uiState.value = state.copy(isArchived = true, isUnstable = false)
        }
    }
}