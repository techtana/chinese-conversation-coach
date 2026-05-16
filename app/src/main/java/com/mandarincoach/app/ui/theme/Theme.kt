package com.mandarincoach.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = ChineseRed,
    onPrimary = SurfaceWhite,
    primaryContainer = Color(0xFFFDECEA),
    onPrimaryContainer = ChineseRedDark,
    secondary = GoldAccent,
    onSecondary = SurfaceWhite,
    background = BackgroundWarm,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceGray,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFD4D0C8),
    error = ListeningRed
)

@Composable
fun MandarinCoachTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
