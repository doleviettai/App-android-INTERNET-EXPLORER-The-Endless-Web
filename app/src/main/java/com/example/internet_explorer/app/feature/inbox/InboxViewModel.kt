package com.example.internet_explorer.app.feature.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.internet_explorer.app.data.repository.GameStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class EmailUiModel(
    val id: String,
    val subject: String,
    val senderName: String,
    val body: String,
    val unlocked: Boolean
)

data class InboxUiState(
    val selectedFolder: String = "inbox",
    val emails: List<EmailUiModel> = emptyList()
)

class InboxViewModel : ViewModel() {

    private val _selectedFolder = MutableStateFlow("inbox")

    // Kết hợp folder đang chọn + progress -- để email secret tự hiện ra ngay khi
    // puzzle liên quan được giải, không cần người chơi thoát vào lại màn hình.
    val uiState: StateFlow<InboxUiState> = combine(
        _selectedFolder,
        GameStateRepository.progress
    ) { folder, _ ->
        val emails = GameStateRepository.getEmails(folder).map { email ->
            EmailUiModel(
                id = email.id,
                subject = email.subject,
                senderName = email.senderName,
                body = email.body,
                unlocked = GameStateRepository.isEmailUnlocked(email)
            )
        }
        InboxUiState(selectedFolder = folder, emails = emails)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InboxUiState())

    fun selectFolder(folder: String) {
        _selectedFolder.value = folder
    }
}