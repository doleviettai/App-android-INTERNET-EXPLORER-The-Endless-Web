package com.example.internet_explorer.app.ui.theme

import androidx.compose.ui.graphics.Color

// Bảng màu Terminal Noir chính thức từ design.md
val BgPrimary = Color(0xFF080A09)
val BgSurface = Color(0xFF0F1412)
val BgSurfaceRaised = Color(0xFF161C19)

val TextPrimary = Color(0xFFC8FFC0)
val TextNoise = Color(0xFF8FA69C)
val TextMuted = Color(0xFF5C6B63)
val TextMutedReadable = Color(0xFF7C8D84)

val AccentTerminal = Color(0xFF39FF88)
val AccentAmber = Color(0xFFFFB000)
val AccentGlitch = Color(0xFFFF3B4E)

val BorderAscii = Color(0xFF2A332E)
val BorderActive = Color(0xFF39FF88)

// Các alias tương thích ngược nếu cần thiết (để build không lỗi trước khi cập nhật toàn bộ)
val BackgroundDark = BgPrimary
val SurfaceDark = BgSurface
val SurfaceVariantDark = BgSurfaceRaised
val OutlineDark = BorderAscii
val AccentGreen = AccentTerminal
val AccentGreenContainer = BgSurface
val ErrorRed = AccentGlitch
val TextSecondary = TextNoise