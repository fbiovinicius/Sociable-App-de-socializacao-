package com.example.sociable.ui.common

import com.example.sociable.core.time.ManausTime
import java.time.Instant
import java.util.Locale

val BrLocale: Locale = Locale.forLanguageTag("pt-BR")

fun formatPrice(price: Double): String = String.format(BrLocale, "R$ %.2f", price)

fun formatCoordinates(latitude: Double, longitude: Double): String =
    String.format(BrLocale, "%.4f, %.4f", latitude, longitude)

fun formatAverage(average: Double): String = String.format(BrLocale, "%.1f", average)

fun formatPriceText(isFree: Boolean, price: Double?): String =
    if (isFree) "Gratuito" else formatPrice(price ?: 0.0)

fun formatSpots(goingCount: Int, capacity: Int?): String =
    if (capacity != null) "$goingCount de $capacity vagas" else "$goingCount confirmados"

fun formatEventDay(startAt: Instant): String =
    "${ManausTime.weekday(startAt)} · ${ManausTime.date(startAt)}"

fun String.capitalizeFirst(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(BrLocale) else it.toString() }
