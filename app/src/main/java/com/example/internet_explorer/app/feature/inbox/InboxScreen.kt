package com.example.internet_explorer.app.feature.inbox

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.internet_explorer.app.ui.components.TerminalCard

private val FOLDERS = listOf("inbox" to "Inbox", "secret" to "Secret")

@Composable
fun InboxScreen(viewModel: InboxViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedIndex = FOLDERS.indexOfFirst { it.first == uiState.selectedFolder }.coerceAtLeast(0)

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedIndex) {
            FOLDERS.forEachIndexed { index, (key, label) ->
                Tab(
                    selected = index == selectedIndex,
                    onClick = { viewModel.selectFolder(key) },
                    text = { Text(label) }
                )
            }
        }

        LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
            items(uiState.emails) { email ->
                TerminalCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "> ${email.senderName}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                            if (!email.unlocked) {
                                Spacer(Modifier.width(6.dp))
                                Text("🔒", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text(
                            email.subject,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (email.unlocked) email.body else "Nội dung này chưa được mở khóa. Giải puzzle liên quan để đọc.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (email.unlocked) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )
                    }
                }
            }
            if (uiState.emails.isEmpty()) {
                item { Text("Không có email nào trong mục này.") }
            }
        }
    }
}