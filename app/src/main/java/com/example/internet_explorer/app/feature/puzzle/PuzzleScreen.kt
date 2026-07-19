package com.example.internet_explorer.app.feature.puzzle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PuzzleScreen(
    viewModel: PuzzleViewModel = viewModel(),
    onViewNotebook: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "> ${uiState.title}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            uiState.hintText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(Modifier.weight(1f)) {
            items(uiState.logLines) { line ->
                val selected = line.id in uiState.selectedIds
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .border(
                            width = 1.dp,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            shape = RoundedCornerShape(2.dp)
                        )
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { viewModel.toggleLine(line.id) }
                        .padding(8.dp)
                ) {
                    Text(
                        text = (if (selected) "> " else "  ") + line.text,
                        fontFamily = FontFamily.Monospace,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { viewModel.submit() },
            enabled = uiState.selectedIds.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("[ NỘP PHÂN TÍCH ]")
        }

        uiState.result?.let { result ->
            Spacer(Modifier.height(12.dp))
            if (result.correct) {
                Text(
                    "✓ Chính xác! Manh mối mới: ${result.unlockedClue?.description}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onViewNotebook, modifier = Modifier.fillMaxWidth()) {
                    Text("[ XEM TRONG NOTEBOOK → ]")
                }
            } else {
                Text(
                    "✗ Chưa đúng. Thử xem lại các dòng có kết quả bất thường (DENIED rồi OK, method lạ...).",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}