package com.example.internet_explorer.app.feature.notebook

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.internet_explorer.app.ui.components.TerminalCard

@Composable
fun NotebookScreen(viewModel: NotebookViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "> NOTEBOOK.LOG",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text("tiến_độ_điều_tra: ${uiState.progressPercent}%", style = MaterialTheme.typography.bodyMedium)
        LinearProgressIndicator(
            progress = { uiState.progressPercent / 100f },
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn {
            if (uiState.unlockedClues.isNotEmpty()) {
                item {
                    Text(
                        "> MANH_MỐI",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                }
                items(uiState.unlockedClues) { clue ->
                    TerminalCard(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(clue.description, Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }

            item {
                Text(
                    "> SỰ_KIỆN_ĐÃ_THU_THẬP",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
            }
            items(uiState.collectedFacts) { fact ->
                Text(
                    "• ${fact.factText}",
                    Modifier.padding(vertical = 4.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (uiState.collectedFacts.isEmpty() && uiState.unlockedClues.isEmpty()) {
                item {
                    Text(
                        "Chưa có manh mối nào. Hãy khám phá vài website để bắt đầu.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}