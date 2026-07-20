package com.example.internet_explorer.app.feature.notebook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.internet_explorer.app.ui.components.AddressBar
import com.example.internet_explorer.app.ui.components.AsciiProgressBar
import com.example.internet_explorer.app.ui.components.BracketCard
import com.example.internet_explorer.app.ui.theme.*

@Composable
fun NotebookScreen(viewModel: NotebookViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Thanh địa chỉ cho nhật ký điều tra
        AddressBar(
            command = "notebook_loader --log",
            isVerified = true
        )

        // Bảng trạng thái tiến độ sử dụng AsciiProgressBar theo yêu cầu GDD
        BracketCard(isFocused = true) {
            Column {
                Text(
                    text = "TIẾN TRÌNH KHỞI KHẢO:",
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentTerminal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                AsciiProgressBar(
                    progress = uiState.progressPercent / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.unlockedClues.isNotEmpty()) {
                item {
                    Text(
                        text = "> MANH_MỐI_ĐÃ_MỞ_KHÓA.TXT",
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentTerminal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                }
                items(uiState.unlockedClues) { clue ->
                    BracketCard {
                        Text(
                            text = clue.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                    }
                }
            }

            if (uiState.collectedFacts.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "> SỰ_THẬT_THU_THẬP.LOG",
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                }
                items(uiState.collectedFacts) { fact ->
                    BracketCard {
                        Text(
                            text = "• ${fact.factText}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMutedReadable
                        )
                    }
                }
            }

            if (uiState.collectedFacts.isEmpty() && uiState.unlockedClues.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "CHƯA CÓ MANH MỐI GHI NHẬN. HÃY KHÁM PHÁ CÁC ĐỊA CHỈ WEB ĐỂ THU THẬP DỮ LIỆU.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMutedReadable,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}