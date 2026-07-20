package com.example.internet_explorer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.internet_explorer.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun BootSequenceScreen(onFinished: () -> Unit) {
    val lines = listOf(
        "> INIT EXPLORER_SESSION...",
        "> HANDSHAKE nexus.core [OK]",
        "> ACCESS LEVEL: UNVERIFIED",
        "> WARNING: connection not authorized by NEXUS",
        "> proceeding anyway..."
    )

    var visibleLinesCount by remember { mutableIntStateOf(0) }
    var currentLineText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        for (i in lines.indices) {
            val line = lines[i]
            visibleLinesCount = i
            currentLineText = ""
            for (charIdx in 1..line.length) {
                delay(12) // Tốc độ chạy chữ 12ms/ký tự
                currentLineText = line.substring(0, charIdx)
            }
            delay(350) // Dừng một lát sau mỗi dòng
        }
        visibleLinesCount = lines.size
        delay(600)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .clickable { onFinished() } // Nhấn chạm để bỏ qua
            .padding(24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (i in 0 until visibleLinesCount) {
                val line = lines[i]
                val color = when {
                    line.contains("[OK]") -> AccentTerminal
                    line.contains("WARNING") || line.contains("UNVERIFIED") -> AccentAmber
                    else -> TextPrimary
                }
                Text(
                    text = line,
                    color = color,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace
                )
            }
            if (visibleLinesCount < lines.size) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val color = when {
                        currentLineText.contains("[OK]") -> AccentTerminal
                        currentLineText.contains("WARNING") || currentLineText.contains("UNVERIFIED") -> AccentAmber
                        else -> TextPrimary
                    }
                    Text(
                        text = currentLineText,
                        color = color,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace
                    )
                    BlinkingCursor()
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "> READY. TAP TO ENTER.",
                        color = AccentTerminal,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    BlinkingCursor()
                }
            }
        }
    }
}
