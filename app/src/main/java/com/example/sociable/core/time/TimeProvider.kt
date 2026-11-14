package com.example.sociable.core.time

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Fonte de tempo injetável para permitir testes determinísticos. */
fun interface TimeProvider {
    fun now(): Instant
}

object SystemTimeProvider : TimeProvider {
    override fun now(): Instant = Instant.now()
}

object ManausTime {
    val ZONE: ZoneId = ZoneId.of("America/Manaus")

    private val LOCALE_BR = Locale.forLanguageTag("pt-BR")
    private val DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", LOCALE_BR).withZone(ZONE)
    private val DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE_BR).withZone(ZONE)
    private val TIME = DateTimeFormatter.ofPattern("HH:mm", LOCALE_BR).withZone(ZONE)
    private val WEEKDAY = DateTimeFormatter.ofPattern("EEE", LOCALE_BR).withZone(ZONE)

    fun dateTime(instant: Instant): String = DATE_TIME.format(instant)
    fun date(instant: Instant): String = DATE.format(instant)
    fun time(instant: Instant): String = TIME.format(instant)
    fun weekday(instant: Instant): String = WEEKDAY.format(instant).replaceFirstChar { it.uppercase() }
}
