package com.example.sociable.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SociableColorScheme = lightColorScheme(
    primary = Coral,
    onPrimary = SCream,
    primaryContainer = OrangeSoft,
    onPrimaryContainer = SInk,
    secondary = Plum,
    onSecondary = SCream,
    secondaryContainer = Rose,
    onSecondaryContainer = SInk,
    background = SCream,
    onBackground = SInk,
    surface = Color.White,
    onSurface = SInk,
    surfaceVariant = Rose,
    onSurfaceVariant = Plum,
    outline = SInk,
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
