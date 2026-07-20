package com.example.internet_explorer.app.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.internet_explorer.app.ui.components.BracketCard
import com.example.internet_explorer.app.ui.components.StatusTag
import com.example.internet_explorer.app.ui.theme.*

@Composable
fun HomeScreen(
    onWebsiteClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "> ls active_case/network_logs/",
                    style = MaterialTheme.typography.titleLarge,
                    color = AccentTerminal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "DỮ LIỆU ĐƯỢC PHỤC HỒI BỞI NEXUS-7",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextNoise
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        items(uiState.items) { item ->
            val site = item.website
            // Định nghĩa kiểu layout để hiển thị dạng nhãn hệ thống
            val typeTag = when (site.layoutType) {
                "company_portfolio" -> "SYS_PORTFOLIO"
                "wiki_archive" -> "WIKI_ARCHIVE"
                "forum" -> "FORUM_LOGS"
                "social_media" -> "SOC_MEDIA"
                else -> "UNKNOWN"
            }

            // Hộp ngoặc nhọn BracketCard
            BracketCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onWebsiteClick(site.id) },
                isFocused = !item.visited // Làm sáng các trang chưa xem
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "[FILE] ${site.url}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (item.visited) TextMutedReadable else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TYPE: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = typeTag,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMutedReadable
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    if (item.visited) {
                        StatusTag(
                            text = "VISITED",
                            color = AccentTerminal.copy(alpha = 0.6f)
                        )
                    } else {
                        StatusTag(
                            text = "UNREAD",
                            color = AccentAmber
                        )
                    }
                }
            }
        }

        if (uiState.items.isEmpty()) {
            item {
                Text(
                    text = "NO NETWORK NODES DETECTED.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AccentGlitch,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}