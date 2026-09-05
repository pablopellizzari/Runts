package com.example.runts.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RuntsDarkColorScheme = darkColorScheme(
    primary = RuntsRedPrimary,
    onPrimary = TextWhite,
    secondary = RuntsRedPrimary,
    onSecondary = TextWhite,
    background = RuntsDarkBackground,
    onBackground = TextWhite,
    surface = RuntsDarkSurface,
    onSurface = TextWhite,
    surfaceVariant = RuntsDarkCard,
    onSurfaceVariant = TextGray,
    outline = RuntsBorderColor
)

@Composable
fun RuntsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RuntsDarkColorScheme,
        typography = Typography,
        content = content
    )
}
