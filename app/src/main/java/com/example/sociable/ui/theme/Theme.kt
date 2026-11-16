package com.example.sociable.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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

private val SociableDarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkOnAccent,
    primaryContainer = DarkAccentSoft,
    onPrimaryContainer = DarkInk,
    secondary = DarkMuted,
    onSecondary = DarkBackground,
    secondaryContainer = DarkChip,
    onSecondaryContainer = DarkInk,
    background = DarkBackground,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkChip,
    onSurfaceVariant = DarkMuted,
    outline = DarkInk,
    error = Color(0xFFFFB4AB),
)

/**
 * O app está no escuro? Considera o modo escolhido no Perfil → Aparência
 * ([ThemeController]); em "Sistema", segue a configuração do aparelho.
 * O `MapScreen` usa o mesmo critério para estilizar o Google Maps.
 */
@Composable
fun isSociableDarkTheme(): Boolean = when (ThemeController.mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/**
 * Tema do Sociable. Segue o modo escolhido pela pessoa (sistema/claro/escuro);
 * os tokens consumidos pelas telas ficam em [SociableTheme]
 * (arquivo SociableColors.kt).
 */
@Composable
fun SociableTheme(content: @Composable () -> Unit) {
    val dark = isSociableDarkTheme()

    CompositionLocalProvider(
        LocalSociableColors provides if (dark) darkSociableColors() else lightSociableColors(),
    ) {
        MaterialTheme(
            colorScheme = if (dark) SociableDarkColorScheme else SociableColorScheme,
            typography = Typography,
            content = content,
        )
    }
}
