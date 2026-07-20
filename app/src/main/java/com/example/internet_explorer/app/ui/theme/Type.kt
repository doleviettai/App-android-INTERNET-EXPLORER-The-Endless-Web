package com.example.internet_explorer.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Monospace TOÀN BỘ để đúng phong cách terminal cổ điển, lạnh lùng.
 * Kích thước đã tối ưu cho điện thoại Android (nhỏ gọn, dễ đọc).
 */
val InternetExplorerTypography = Typography(
    // Display (Boot sequence, case-closed) -> dùng ít, kích thước vừa phải
    displayMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 1.sp
    ),
    // Title Large (Screen headers) -> thu nhỏ từ 20sp xuống 16sp
    titleLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 0.8.sp
    ),
    // Title Medium (các tiêu đề con)
    titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.4.sp
    ),
    // Title Small (các tiêu đề khối nhỏ)
    titleSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 0.3.sp
    ),
    // Body Large (Primary reading content) -> thu nhỏ từ 15sp xuống 13sp
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        letterSpacing = 0.15.sp,
        lineHeight = 20.sp
    ),
    // Body Medium
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.15.sp,
        lineHeight = 18.sp
    ),
    // Body Small / Code / Log (Puzzle log lines)
    bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        letterSpacing = 0.sp,
        lineHeight = 14.sp
    ),
    // Label Large (Eyebrows, nav items, tags)
    labelLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 1.2.sp
    ),
    // Label Small / Caption (Timestamps, ref IDs)
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
        letterSpacing = 0.8.sp
    ),
    // Headline Medium (tương thích ngược)
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 0.8.sp
    ),
    // Headline Small (tương thích ngược)
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 0.4.sp
    )
)