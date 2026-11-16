package com.example.sociable.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta do protótipo (creme + coral + roxo-noite)
val SCream = Color(0xFFFBF8F5)
val Rose = Color(0xFFF6E8E2)
val SSand = Color(0xFFE6C4B0)
val Peach = Color(0xFFE3AFA0)
val Coral = Color(0xFFD44D5C)
val SWine = Color(0xFF70313F)
val Plum = Color(0xFF7B5A7E)
val SInk = Color(0xFF1E0B2E)
val Orange = Color(0xFFF2A057)
val OrangeSoft = Color(0xFFFCE3C8)
val Star = Color(0xFFF2B705)

// Nomes usados pelo MaterialTheme e por telas antigas.
val Cream = SCream
val CreamDark = Rose
val Wine = Coral
val WineDark = SWine
val DarkPurple = SInk
val Ink = SInk
val MutedInk = Plum
val Sand = SSand
val SoftPink = Rose
val SuccessGreen = Color(0xFF2E7D32)

private val ErrorRed = Color(0xFFB3261E)

// Paleta do modo escuro: mesma identidade adesiva, superfícies em roxo-noite.
val DarkBackground = Color(0xFF171219)
val DarkSurface = Color(0xFF251D2E)
val DarkChip = Color(0xFF2F2539)
val DarkAccentSoft = Color(0xFF473040)
val DarkInk = Color(0xFFF4EAF6)
val DarkMuted = Color(0xFFC3A9CA)
val DarkAccent = Color(0xFFDE5A67)
val DarkOnAccent = Color(0xFFFFF4F1)
val DarkOutlineSoft = Color(0xFF544459)
val DarkPeach = Color(0xFF5D4753)

fun lightSociableColors() = SociableColors(
    background = SCream,
    surface = Color.White,
    chip = Rose,
    accentSoft = OrangeSoft,
    ink = SInk,
    shadow = SInk,
    muted = Plum,
    accent = Coral,
    onAccent = SCream,
    outlineSoft = SSand,
    peach = Peach,
    star = Star,
    error = ErrorRed,
)

fun darkSociableColors() = SociableColors(
    background = DarkBackground,
    surface = DarkSurface,
    chip = DarkChip,
    accentSoft = DarkAccentSoft,
    ink = DarkInk,
    shadow = Color.Black,
    muted = DarkMuted,
    accent = DarkAccent,
    onAccent = DarkOnAccent,
    outlineSoft = DarkOutlineSoft,
    peach = DarkPeach,
    star = Star,
    error = Color(0xFFFFB4AB),
)
