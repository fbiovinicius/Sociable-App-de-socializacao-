package com.example.sociable.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SociableColorScheme = lightColorScheme(
    primary = Wine,
    onPrimary = Color.White,
    primaryContainer = SoftPink,
    onPrimaryContainer = WineDark,
    secondary = DarkPurple,
    onSecondary = Color.White,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = CreamDark,
    onSurfaceVariant = MutedInk,
    outline = Sand,
    error = Color(0xFFB3261E),
)

@Composable
fun SociableTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SociableColorScheme,
        typography = Typography,
        content = content,
    )
}
