package com.example.sociable.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SociableScheme = lightColorScheme(
    primary = Coral,
    onPrimary = Cream,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    secondary = Plum,
    tertiary = Orange,
)

@Composable
fun SociableTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SociableScheme,
        content = content
    )
}