package com.example.internet_explorer.app.feature.puzzle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.internet_explorer.app.ui.components.AddressBar
import com.example.internet_explorer.app.ui.components.BracketCard
import com.example.internet_explorer.app.ui.components.TerminalButton
import com.example.internet_explorer.app.ui.theme.*

// Chiều rộng cố định của bảng log -- đủ cho dòng log dài nhất hiện có (~70 ký tự monospace).
// Mọi dòng đều lấy đúng chiều rộng này nên cuộn ngang luôn đồng bộ tuyệt đối giữa các dòng.
// Nếu sau này log dài hơn hẳn, tăng giá trị này lên tương ứng.
private val LOG_TABLE_WIDTH = 620.dp

@Composable
fun PuzzleScreen(
    viewModel: PuzzleViewModel = viewModel(),
    onViewNotebook: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Thanh địa chỉ cho công cụ phân tích log
        AddressBar(
            command = "sys_log_analyzer --case=${uiState.title.replace(" ", "_")}",
            isVerified = false
        )

        // Hướng dẫn giải puzzle bọc trong card phụ
        BracketCard {
            Text(
                text = "HƯỚNG DẪN PHÂN TÍCH:",
                style = MaterialTheme.typography.labelLarge,
                color = AccentAmber,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = uiState.hintText,
                style = MaterialTheme.typography.bodyLarge,
                color = TextMutedReadable
            )
        }

        // Bảng log danh sách -- cuộn ngang đồng bộ toàn bảng + cuộn dọc ảo hóa (LazyColumn)
        BracketCard(
            modifier = Modifier.weight(1f),
            isFocused = true
        ) {
            Column(Modifier.fillMaxSize()) {
                if (horizontalScrollState.maxValue > 0) {
                    Text(
                        text = "◀ VUỐT NGANG ĐỂ XEM ĐẦY ĐỦ ▶",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedReadable
                    )
                    Spacer(Modifier.height(4.dp))
                }

                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .width(LOG_TABLE_WIDTH)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uiState.logLines) { line ->
                            val selected = line.id in uiState.selectedIds
                            LogLineRow(
                                text = line.text,
                                selected = selected,
                                onClick = { viewModel.toggleLine(line.id) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                HorizontalScrollbar(
                    scrollState = horizontalScrollState,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Vùng hiển thị kết quả phân tích
        uiState.result?.let { result ->
            BracketCard(isFocused = true) {
                if (result.correct) {
                    Text(
                        text = ">>> PHÂN TÍCH CHÍNH XÁC. DỮ LIỆU ĐƯỢC GIẢI MÃ.",
                        color = AccentTerminal,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Manh mối mới: ${result.unlockedClue?.description}",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    TerminalButton(
                        text = "[ XEM TRONG NOTEBOOK → ]",
                        onClick = onViewNotebook,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Thiết kế lại thông báo lỗi lạnh lùng, forensics, không xin lỗi theo GDD mục 10
                    Text(
                        text = ">>> LỖI: PHÂN TÍCH SAI LỆCH. KẾT QUẢ KHÔNG KHỚP.",
                        color = AccentGlitch,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Hành động: Hãy rà soát lại các dòng log có sự bất thường (ví dụ: FAILED liên tục rồi thành công, phương thức truy cập lạ, hoặc tài khoản người đã nghỉ việc).",
                        color = TextMutedReadable,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Nút nộp bài toán
        if (uiState.result?.correct != true) {
            TerminalButton(
                text = "[ GỬI KẾT QUẢ PHÂN TÍCH ]",
                onClick = { viewModel.submit() },
                enabled = uiState.selectedIds.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Vẽ từng dòng log chi tiết. Không còn cắt chữ bằng ellipsis -- LazyColumn cha đã có
 * chiều rộng cố định LOG_TABLE_WIDTH, người chơi cuộn ngang để đọc trọn dòng dài.
 * Đáp ứng yêu cầu 9.3: dùng underline cho timestamp khi selected để hỗ trợ người mù màu.
 */
@Composable
private fun LogLineRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    // Tách mốc thời gian để vẽ gạch chân
    val parts = text.split(Regex("\\s+"), limit = 2)
    val timestamp = parts.getOrNull(0) ?: ""
    val logContent = parts.getOrNull(1) ?: ""

    val textColor = if (selected) AccentAmber else TextMutedReadable
    val timestampDecoration = if (selected) TextDecoration.Underline else TextDecoration.None
    val backgroundColor = if (selected) AccentAmber.copy(alpha = 0.08f) else Color.Transparent

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
            color = AccentAmber,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )

        // Thời gian có gạch chân nếu được chọn (Hỗ trợ mù màu)
        Text(
            text = timestamp,
            color = textColor,
            style = MaterialTheme.typography.bodySmall.copy(textDecoration = timestampDecoration),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace
        )

        Spacer(Modifier.width(8.dp))

        // Không giới hạn maxLines/overflow nữa -- dòng dài sẽ hiện trọn vẹn,
        // người chơi cuộn ngang (Box cha) để đọc hết thay vì bị cắt "...".
        Text(
            text = logContent,
            color = if (selected) AccentAmber else TextPrimary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}

/**
 * Thanh chỉ báo cuộn ngang tự vẽ bằng Canvas -- Compose chưa có scrollbar ngang
 * dựng sẵn ổn định trong material3, nên vẽ tay 1 track mờ + 1 thumb theo tỉ lệ
 * scrollState.value / scrollState.maxValue. Tự ẩn khi không có gì để cuộn.
 */
@Composable
private fun HorizontalScrollbar(
    scrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier
) {
    if (scrollState.maxValue <= 0) return

    Canvas(
        modifier = modifier.height(4.dp)
    ) {
        val trackY = size.height / 2
        drawLine(
            color = TextMutedReadable.copy(alpha = 0.25f),
            start = Offset(0f, trackY),
            end = Offset(size.width, trackY),
            strokeWidth = size.height
        )

        val thumbWidthRatio = 0.25f
        val thumbWidth = size.width * thumbWidthRatio
        val progress = scrollState.value.toFloat() / scrollState.maxValue.toFloat()
        val thumbStart = (size.width - thumbWidth) * progress

        drawLine(
            color = AccentAmber,
            start = Offset(thumbStart, trackY),
            end = Offset(thumbStart + thumbWidth, trackY),
            strokeWidth = size.height
        )
    }
}