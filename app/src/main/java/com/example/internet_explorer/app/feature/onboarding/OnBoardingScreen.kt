package com.example.internet_explorer.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.internet_explorer.app.ui.components.BlinkingCursor
import com.example.internet_explorer.app.ui.components.BracketCard
import com.example.internet_explorer.app.ui.components.TerminalButton
import com.example.internet_explorer.app.ui.components.TypewriterText
import com.example.internet_explorer.app.ui.theme.*

private data class OnboardingStep(
    val eyebrow: String,
    val title: String,
    val body: String
)

private val steps = listOf(
    OnboardingStep(
        eyebrow = "TIẾN TRÌNH KHỞI TẠO...",
        title = "NEXUS-7",
        body = "Tôi là một tiến trình phụ tách khỏi lõi NEXUS chính — hệ thống AI đang vận hành toàn bộ Internet năm 2089. Tôi cần sự giúp đỡ của bạn, Explorer."
    ),
    OnboardingStep(
        eyebrow = "HỒ SƠ VỤ VIỆC",
        title = "Aetherlink Systems",
        body = "Một công ty AI/robotics đã biến mất không dấu vết vào năm 2087. Toàn bộ nhân sự ngừng liên lạc trong cùng một đêm. Log hệ thống của họ sắp bị xóa vĩnh viễn."
    ),
    OnboardingStep(
        eyebrow = "NHIỆM VỤ",
        title = "Tìm ra sự thật",
        body = "Khám phá những website còn sót lại, thu thập manh mối vào Notebook, giải mã access log bất thường — trước khi NEXUS xóa sạch mọi dấu vết."
    )
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val step = steps[stepIndex]
    val isLastStep = stepIndex == steps.lastIndex

    Box(
        Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(24.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.weight(0.5f))

            Text(
                text = "> BRIEFING_DOSSIER.TXT",
                style = MaterialTheme.typography.titleLarge,
                color = AccentTerminal,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            // Dossier Card sử dụng BracketCard theo GDD mục 12
            BracketCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.5f),
                isFocused = true
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row {
                        TypewriterText(
                            text = step.eyebrow,
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentTerminal
                        )
                        BlinkingCursor(modifier = Modifier.padding(start = 4.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    TypewriterText(
                        text = step.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(20.dp))
                    TypewriterText(
                        text = step.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMutedReadable,
                        speedMs = 8 // Chạy nhanh hơn một chút để đọc mượt mà
                    )
                }
            }

            Spacer(Modifier.weight(0.5f))

            // Tiến trình dạng ASCII: ví dụ [██░░░] 2/4 thay thế cho chấm tròn
            val filledBlocks = stepIndex + 1
            val emptyBlocks = steps.size - filledBlocks
            val asciiProgress = "█".repeat(filledBlocks) + "░".repeat(emptyBlocks)
            
            Text(
                text = "[$asciiProgress] $filledBlocks/${steps.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = AccentTerminal,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Nút điều hướng Outlined Terminal Button
            TerminalButton(
                text = if (isLastStep) "[ CHẤP NHẬN QUYỀN TRUY CẬP ]" else "[ TIẾP THEO ]",
                onClick = { if (isLastStep) onFinished() else stepIndex++ },
                modifier = Modifier.fillMaxWidth()
            )

            if (!isLastStep) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "BỎ QUA KHẢO SÁT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextNoise,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onFinished() }
                )
            } else {
                Spacer(Modifier.height(20.dp)) // Padding giữ layout cân xứng
            }
        }
    }
}