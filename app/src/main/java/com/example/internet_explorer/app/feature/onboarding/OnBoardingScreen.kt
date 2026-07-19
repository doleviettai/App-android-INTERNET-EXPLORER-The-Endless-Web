package com.example.internet_explorer.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.internet_explorer.app.ui.components.BlinkingCursor

private data class OnboardingStep(
    val eyebrow: String,
    val title: String,
    val body: String
)

private val steps = listOf(
    OnboardingStep(
        eyebrow = "TIẾN TRÌNH KHỞI TẠO...",
        title = "NEXUS-7",
        body = "Tôi là một tiến trình phụ tách khỏi lõi NEXUS chính — hệ thống AI đang vận hành " +
                "toàn bộ Internet năm 2089. Tôi cần sự giúp đỡ của bạn, Explorer."
    ),
    OnboardingStep(
        eyebrow = "HỒ SƠ VỤ VIỆC",
        title = "Aetherlink Systems",
        body = "Một công ty AI/robotics đã biến mất không dấu vết vào năm 2087. Toàn bộ nhân sự " +
                "ngừng liên lạc trong cùng một đêm. Log hệ thống của họ sắp bị xóa vĩnh viễn."
    ),
    OnboardingStep(
        eyebrow = "NHIỆM VỤ",
        title = "Tìm ra sự thật",
        body = "Khám phá những website còn sót lại, thu thập manh mối vào Notebook, giải mã access " +
                "log bất thường — trước khi NEXUS xóa sạch mọi dấu vết."
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
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.weight(1f))

            Row {
                Text(
                    step.eyebrow,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
                BlinkingCursor(modifier = Modifier.padding(start = 4.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                step.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                step.body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.weight(1f))

            // Chấm tiến độ 3 bước
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                steps.indices.forEach { index ->
                    Box(
                        Modifier
                            .height(4.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (index <= stepIndex) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { if (isLastStep) onFinished() else stepIndex++ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    if (isLastStep) "[ BẮT ĐẦU ĐIỀU TRA → ]" else "[ TIẾP THEO ]",
                    fontWeight = FontWeight.Bold
                )
            }

            if (!isLastStep) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Bỏ qua",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onFinished() }
                )
            }
        }
    }
}