package com.example.internet_explorer.app.feature.inbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.internet_explorer.app.ui.components.AddressBar
import com.example.internet_explorer.app.ui.components.BracketCard
import com.example.internet_explorer.app.ui.components.StatusTag
import com.example.internet_explorer.app.ui.theme.*

private val FOLDERS = listOf("inbox" to "Inbox", "secret" to "Secret")

@Composable
fun InboxScreen(viewModel: InboxViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // Theo dõi danh sách email đã được đọc/mở ra trong phiên chơi hiện tại
    var readEmailIds by remember { mutableStateOf(setOf<String>()) }
    var expandedEmailIds by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Thanh địa chỉ hòm thư
        AddressBar(
            command = "mailbox --folder=${uiState.selectedFolder}",
            isVerified = true
        )

        // Custom TabRow dạng hộp phẳng không bo tròn Material
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderAscii)
                .background(BgSurface)
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FOLDERS.forEach { (key, label) ->
                val selected = uiState.selectedFolder == key
                val text = if (selected) "[ ${label.toUpperCase()} ]" else "  ${label.toUpperCase()}  "
                val textColor = if (selected) AccentTerminal else TextMuted

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { viewModel.selectFolder(key) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        color = textColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Danh sách email
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.emails) { email ->
                val isExpanded = email.id in expandedEmailIds
                val isRead = email.id in readEmailIds || !email.unlocked
                val subjectColor = if (isRead) TextMutedReadable else TextPrimary

                BracketCard(isFocused = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (email.unlocked) {
                                    expandedEmailIds = if (isExpanded) {
                                        expandedEmailIds - email.id
                                    } else {
                                        expandedEmailIds + email.id
                                    }
                                    readEmailIds = readEmailIds + email.id
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "> FROM: ${email.senderName.toUpperCase()}",
                                    color = if (isExpanded) AccentTerminal else TextNoise,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                // Dấu chấm cảnh báo chưa đọc Amber
                                if (!isRead && email.unlocked) {
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(AccentAmber)
                                    )
                                }
                            }
                            if (!email.unlocked) {
                                StatusTag(text = "LOCKED", color = AccentAmber)
                            } else {
                                StatusTag(text = if (isRead) "READ" else "NEW", color = if (isRead) TextMuted else AccentTerminal)
                            }
                        }
                        
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = email.subject,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = subjectColor
                        )

                        AnimatedVisibility(visible = isExpanded) {
                            Column {
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(BorderAscii)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = email.body,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.emails.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO EMAIL MESSAGE FOUND.",
                            color = TextMutedReadable,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}