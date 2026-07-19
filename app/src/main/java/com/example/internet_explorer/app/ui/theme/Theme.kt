package com.example.internet_explorer.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val InternetExplorerColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = BackgroundDark,
    primaryContainer = AccentGreenContainer,
    onPrimaryContainer = AccentGreen,
    secondary = AccentAmber,
    onSecondary = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = OutlineDark,
    error = ErrorRed,
    onError = BackgroundDark
)

// Góc gần vuông cho MỌI component mặc định (Button, Card, NavigationBar...) --
// đây là điểm khác biệt lớn nhất so với bản trước: bo tròn kiểu Material Design
// là thứ phá vỡ cảm giác "cửa sổ terminal" nhanh nhất.
private val InternetExplorerShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(2.dp),
    large = RoundedCornerShape(2.dp),
    extraLarge = RoundedCornerShape(2.dp)
)

@Composable
fun InternetExplorerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = InternetExplorerColorScheme,
        typography = InternetExplorerTypography,
        shapes = InternetExplorerShapes,
        content = content
    )
}