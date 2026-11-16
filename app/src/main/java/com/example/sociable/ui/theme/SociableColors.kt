package com.example.sociable.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Paleta por papel (role). Todas as telas consomem estes tokens via
 * [SociableTheme] — nada importa as cores brutas diretamente. Assim o modo
 * escuro troca a paleta inteira sem tocar nas telas.
 */
@Immutable
data class SociableColors(
    /** Fundo das telas (creme no claro). */
    val background: Color,
    /** Cartões elevados e campos de texto (branco no claro). */
    val surface: Color,
    /** Preenchimento de chips não selecionados e balões de chat. */
    val chip: Color,
    /** Cartões de destaque ("segredinho"). */
    val accentSoft: Color,
    /** Texto principal e contornos do visual adesivo. */
    val ink: Color,
    /** Sombra sólida deslocada do adesivo. */
    val shadow: Color,
    /** Texto secundário. */
    val muted: Color,
    /** Cor de ação (botão primário, chip selecionado). */
    val accent: Color,
    /** Texto sobre [accent]. */
    val onAccent: Color,
    /** Contorno suave de campos de texto. */
    val outlineSoft: Color,
    /** Preenchimento de botão desativado/carregando. */
    val peach: Color,
    /** Estrela de avaliação. */
    val star: Color,
    /** Erro. */
    val error: Color,
)

val LocalSociableColors = staticCompositionLocalOf { lightSociableColors() }

/**
 * Acesso aos tokens. Tanto `SociableTheme { }` (composable, no Theme.kt) quanto
 * `SociableTheme.ink` (propriedades abaixo) compartilham o mesmo nome, como o
 * próprio `MaterialTheme` do Compose.
 */
object SociableTheme {

    val colors: SociableColors
        @Composable @ReadOnlyComposable get() = LocalSociableColors.current

    val background: Color
        @Composable @ReadOnlyComposable get() = colors.background

    val surface: Color
        @Composable @ReadOnlyComposable get() = colors.surface

    val chip: Color
        @Composable @ReadOnlyComposable get() = colors.chip

    val accentSoft: Color
        @Composable @ReadOnlyComposable get() = colors.accentSoft

    val ink: Color
        @Composable @ReadOnlyComposable get() = colors.ink

    val shadow: Color
        @Composable @ReadOnlyComposable get() = colors.shadow

    val muted: Color
        @Composable @ReadOnlyComposable get() = colors.muted

    val accent: Color
        @Composable @ReadOnlyComposable get() = colors.accent

    val onAccent: Color
        @Composable @ReadOnlyComposable get() = colors.onAccent

    val outlineSoft: Color
        @Composable @ReadOnlyComposable get() = colors.outlineSoft

    val peach: Color
        @Composable @ReadOnlyComposable get() = colors.peach

    val star: Color
        @Composable @ReadOnlyComposable get() = colors.star

    val error: Color
        @Composable @ReadOnlyComposable get() = colors.error
}
