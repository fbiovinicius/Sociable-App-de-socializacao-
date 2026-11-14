package com.example.sociable.ui.common

import java.util.Locale

val BrLocale: Locale = Locale.forLanguageTag("pt-BR")

fun formatPrice(price: Double): String = String.format(BrLocale, "R$ %.2f", price)

fun formatCoordinates(latitude: Double, longitude: Double): String =
    String.format(BrLocale, "%.4f, %.4f", latitude, longitude)

fun formatAverage(average: Double): String = String.format(BrLocale, "%.1f", average)
