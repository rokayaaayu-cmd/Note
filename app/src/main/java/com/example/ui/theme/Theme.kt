package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TermuxDarkColorScheme = darkColorScheme(
    primary = TermuxGreen,
    onPrimary = Color(0xFF00210E),
    primaryContainer = TermuxGreenContainer,
    onPrimaryContainer = TermuxGreen,
    secondary = ElectricCyan,
    onSecondary = Color(0xFF001E2F),
    secondaryContainer = ElectricCyanContainer,
    onSecondaryContainer = ElectricCyan,
    tertiary = AmberAlert,
    onTertiary = Color(0xFF261900),
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = AmberAlert,
    background = TerminalBg,
    onBackground = TextPrimary,
    surface = TerminalSurface,
    onSurface = TextPrimary,
    surfaceVariant = TerminalSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = TerminalBorder,
    error = CoralDanger
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TermuxDarkColorScheme,
        typography = Typography,
        content = content
    )
}
