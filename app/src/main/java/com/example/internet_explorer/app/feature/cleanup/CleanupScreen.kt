package com.example.internet_explorer.app.feature.cleanup

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.internet_explorer.app.data.repository.TraceAction
import com.example.internet_explorer.app.data.repository.TraceLogEntry
import com.example.internet_explorer.app.ui.components.AsciiProgressBar
import com.example.internet_explorer.app.ui.components.BracketCard
import com.example.internet_explorer.app.ui.components.EmptyState
import com.example.internet_explorer.app.ui.components.TerminalButton
import com.example.internet_explorer.app.ui.theme.*

/**
 * Màn hình bắt buộc khi Trace vượt ngưỡng nguy hiểm (gameplay-mechanics-phase2.md,
 * mục "Nguy hiểm 0.85-1.0"). Đảo ngược logic Puzzle Log Analysis thường: thay vì tìm
 * bất thường của NGƯỜI KHÁC, người chơi chọn DÒNG LOG CỦA CHÍNH MÌNH cần xóa để hạ
 * Trace xuống mức an toàn. Đây là 1 trong 2 trường hợp CHỦ ĐỘNG hiện số liệu thô
 * (AsciiProgressBar) thay vì chỉ ám chỉ qua overlay -- vì bối cảnh câu chuyện lúc này
 * chính là "bạn đã bị phát hiện", không còn lý do gì để giấu mức độ nguy hiểm nữa.
 */
@Composable
fun CleanupScreen(viewModel: CleanupViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = ">>> KẾT NỐI BỊ NEXUS PHÁT HIỆN",
            style = MaterialTheme.typography.headlineSmall,
            color = AccentGlitch,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Xóa bớt dấu vết hoạt động của bạn trước khi bị ngắt kết nối hoàn toàn. " +
                    "Chọn các dòng log cần xóa rồi gửi lệnh.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMutedReadable
        )

        AsciiProgressBar(
            progress = uiState.currentTrace,
            modifier = Modifier.fillMaxWidth()
        )

        BracketCard(
            modifier = Modifier.weight(1f),
            isFocused = true
        ) {
            if (uiState.logEntries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(message = "KHÔNG CÒN DẤU VẾT NÀO.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(uiState.logEntries) { entry ->
                        CleanupLogRow(
                            entry = entry,
                            selected = entry.id in uiState.selectedIds,
                            onClick = { viewModel.toggleEntry(entry.id) }
                        )
                    }
                }
            }
        }

        uiState.result?.let { result ->
            Text(
                text = if (result.success) {
                    ">>> ĐÃ DỌN SẠCH. KẾT NỐI AN TOÀN TRỞ LẠI."
                } else {
                    ">>> CHƯA ĐỦ. NEXUS VẪN ĐANG THEO DÕI."
                },
                color = if (result.success) AccentTerminal else AccentGlitch,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        TerminalButton(
            text = "[ XÓA CÁC MỤC ĐÃ CHỌN ]",
            onClick = { viewModel.submit() },
            enabled = uiState.selectedIds.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CleanupLogRow(
    entry: TraceLogEntry,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) AccentGlitch else TextPrimary
    val backgroundColor = if (selected) AccentGlitch.copy(alpha = 0.08f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (selected) "> " else "  ",
            color = AccentGlitch,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "[step ${entry.atStep}] ${entry.action.label()}",
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun TraceAction.label(): String = when (this) {
    TraceAction.OPEN_NEW_SITE -> "Truy cập trang mới"
    TraceAction.FAILED_PUZZLE -> "Phân tích log thất bại"
    TraceAction.SUCCESSFUL_HACK -> "Truy cập trái phép thành công"
}