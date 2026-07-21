package com.example.internet_explorer.app.ui.components

import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.internet_explorer.app.data.repository.GameStateRepository
import com.example.internet_explorer.app.ui.theme.*
import kotlinx.coroutines.delay

/** Con trỏ nhấp nháy kiểu terminal cổ điển. */
@Composable
fun BlinkingCursor(
    modifier: Modifier = Modifier,
    color: Color = AccentTerminal
) {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            visible = !visible
        }
    }
    Text(
        "█",
        color = if (visible) color else Color.Transparent,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        modifier = modifier
    )
}

/** Lớp phủ quét CRT tĩnh, rất mờ. */
@Composable
fun ScanlineOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val lineSpacing = 4.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Color.White.copy(alpha = 0.02f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += lineSpacing
        }
    }
}

/**
 * Lớp phủ nhiễu tĩnh động (Integrity Static Overlay) theo tiến trình của case
 * VÀ Trace Meter (gameplay-mechanics-phase2.md mục 0, 2.4) -- 2 nguồn input,
 * cường độ cuối cùng lấy giá trị LỚN HƠN giữa 2 bên (khớp GameStateRepository.currentStaticIntensity()).
 * Khi tiến trình/Trace càng tăng, độ quét CRT càng đậm và thỉnh thoảng có chớp giật màn hình (glitch).
 */
@Composable
fun IntegrityStaticOverlay(
    modifier: Modifier = Modifier
) {
    val progressState by GameStateRepository.progress.collectAsState()
    val traceLevel by GameStateRepository.traceLevel.collectAsState()
    val totalFacts = remember { GameStateRepository.getTotalFactCount() }
    val collectedFacts = progressState.collectedFactIds.size
    val caseProgressFraction = if (totalFacts > 0) collectedFacts.toFloat() / totalFacts.toFloat() else 0f

    // Nguồn thứ 2 (Trace): NEXUS phản ứng nhanh hơn khi người chơi "gây tiếng",
    // không chỉ khi tiến độ case tăng tự nhiên.
    val progressFraction = maxOf(caseProgressFraction, traceLevel)

    // Cường độ scanline thay đổi từ 4% đến 9% theo GDD
    val scanlineAlpha = 0.04f + (progressFraction * 0.05f)

    var isGlitching by remember { mutableStateOf(false) }

    LaunchedEffect(progressFraction) {
        if (progressFraction == 0f) return@LaunchedEffect
        while (true) {
            // Tần suất nhiễu tăng dần khi tiến trình lớn hơn. Chờ từ 0.5s đến 5s.
            val delayMs = (5000 - (progressFraction * 4500)).toLong().coerceAtLeast(500)
            delay(delayMs)

            // Khả năng giật màn hình
            if (Math.random() < 0.2 + (progressFraction * 0.5)) {
                isGlitching = true
                delay(80) // Thời gian giật nhấp nháy 80ms theo thiết kế
                isGlitching = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineSpacing = 4.dp.toPx()
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color.White.copy(alpha = scanlineAlpha),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += lineSpacing
            }

            // Vẽ thêm các chấm/sọc xanh ngẫu nhiên để mô phỏng dữ liệu nhiễu NEXUS
            if (progressFraction > 0.2f) {
                val noiseCount = (progressFraction * 12).toInt()
                for (i in 0 until noiseCount) {
                    val lineY = (Math.random() * size.height).toFloat()
                    val lineX = (Math.random() * size.width).toFloat()
                    val lineWidth = (5 + Math.random() * 30).toFloat()
                    drawLine(
                        color = AccentTerminal.copy(alpha = 0.04f * progressFraction),
                        start = Offset(lineX, lineY),
                        end = Offset(lineX + lineWidth, lineY),
                        strokeWidth = 1f
                    )
                }
            }
        }

        // Chớp màn hình xanh nhẹ mô phỏng glitch
        if (isGlitching) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AccentTerminal.copy(alpha = 0.05f))
            )
        }
    }
}

/** Hiệu ứng máy đánh chữ (Typewriter Text) hỗ trợ tắt animation (Reduced Motion). */
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    speedMs: Long = 14,
    onComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val isReducedMotion = remember {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            scale == 0f
        } catch (e: Exception) {
            false
        }
    }

    var visibleText by remember(text) { mutableStateOf("") }

    LaunchedEffect(text) {
        if (isReducedMotion) {
            visibleText = text
            onComplete()
        } else {
            visibleText = ""
            for (i in 1..text.length) {
                delay(speedMs)
                visibleText = text.substring(0, i)
            }
            onComplete()
        }
    }

    Text(
        text = visibleText,
        modifier = modifier,
        color = color,
        style = style
    )
}

/**
 * Hộp ngoặc nhọn Bracket Card -- Component cốt lõi thay thế cho Card thông thường.
 * Có 4 ký tự ┌ ┐ └ ┘ được đặt tuyệt đối tại 4 góc. Nền tự sáng lên và đổi màu góc khi được chọn/focus.
 */
@Composable
fun BracketCard(
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = if (isFocused) BorderActive else BorderAscii
    val backgroundColor = if (isFocused) BgSurfaceRaised else BgSurface

    Box(
        modifier = modifier
            .background(backgroundColor)
            .border(BorderStroke(1.dp, borderColor))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )

        val cornerColor = if (isFocused) AccentTerminal else TextMuted

        // Vẽ góc ┌ ┐ └ ┘
        Text(
            "┌",
            color = cornerColor,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 1.dp, start = 3.dp)
        )
        Text(
            "┐",
            color = cornerColor,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 1.dp, end = 3.dp)
        )
        Text(
            "└",
            color = cornerColor,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 1.dp, start = 3.dp)
        )
        Text(
            "┘",
            color = cornerColor,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 1.dp, end = 3.dp)
        )
    }
}

/**
 * Address bar hiển thị đường dẫn và trạng thái kết nối.
 * Thêm dòng Trace Meter (gameplay-mechanics-phase2.md mục 2.4) -- KHÔNG hiện số Trace
 * trực tiếp (phá Diegetic UI), chỉ hiện tag [CONNECTION FLAGGED] khi traceLevel >= 0.3,
 * leo màu accent.amber -> accent.glitch ở mức >= 0.6. Tự đọc GameStateRepository.traceLevel
 * ngay trong component (giống cách IntegrityStaticOverlay đã tự đọc progress) nên mọi nơi
 * gọi AddressBar hiện có không cần đổi tham số gì.
 */
@Composable
fun AddressBar(
    command: String,
    modifier: Modifier = Modifier,
    isVerified: Boolean = true
) {
    val statusColor = if (isVerified) AccentTerminal else AccentAmber
    val statusText = if (isVerified) "[CONNECTED]" else "[UNVERIFIED]"

    val traceLevel by GameStateRepository.traceLevel.collectAsState()
    val isFlagged = traceLevel >= 0.3f
    val isDangerFlagged = traceLevel >= 0.6f
    val flagColor = if (isDangerFlagged) AccentGlitch else AccentAmber

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BgSurface)
            .border(BorderStroke(1.dp, BorderAscii))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "> ",
                        color = AccentTerminal,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        command,
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    statusText,
                    color = statusColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isFlagged) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "[CONNECTION FLAGGED]",
                    color = flagColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** Nút Terminal viền mảnh rỗng ruột (outlined only), phản hồi chạm đổi màu nền nhẹ. */
@Composable
fun TerminalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isAmber: Boolean = false
) {
    val color = when {
        !enabled -> TextMuted
        isAmber -> AccentAmber
        else -> AccentTerminal
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val backgroundColor = if (isPressed && enabled) color.copy(alpha = 0.08f) else Color.Transparent

    Box(
        modifier = modifier
            .height(52.dp)
            .background(backgroundColor)
            .border(BorderStroke(1.dp, color))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null, // Triệt tiêu hiệu ứng bong bóng Material
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Nhãn trạng thái viền mảnh 1dp. */
@Composable
fun StatusTag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AccentTerminal
) {
    Box(
        modifier = modifier
            .border(BorderStroke(1.dp, color))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Thanh tiến trình ASCII [████░░░] */
@Composable
fun AsciiProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    totalBlocks: Int = 10
) {
    val filled = (progress * totalBlocks).toInt().coerceIn(0, totalBlocks)
    val empty = totalBlocks - filled
    val bar = "█".repeat(filled) + "░".repeat(empty)

    Text(
        text = "[$bar] ${(progress * 100).toInt()}%",
        color = AccentTerminal,
        style = MaterialTheme.typography.bodyMedium,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

/**
 * Empty state kiểu system message (design-system.md mục 8) -- dùng cho danh sách rỗng
 * VÀ cho site đã bị Site Decay xóa nội dung ("[ARCHIVED BY NEXUS]"). Không có mascot/hình vẽ,
 * chỉ chữ, đúng tinh thần forensic/lạnh lùng của toàn app.
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    flavorText: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = TextMutedReadable,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        if (flavorText != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = flavorText,
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}