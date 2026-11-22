package com.example.sociable.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ManausTimeTest {

    @Test
    fun `horario de Manaus vira instante UTC`() {
        // Manaus fica em UTC-4 o ano inteiro (sem horário de verão).
        val instant = ManausTime.toInstant(
            LocalDate.of(2026, 11, 22),
            LocalTime.of(20, 0),
        )
        assertEquals(Instant.parse("2026-11-23T00:00:00Z"), instant)
    }

    @Test
    fun `conversao de ida e volta preserva data e hora`() {
        val instant = Instant.parse("2026-05-10T17:30:00Z")
        assertEquals(LocalDate.of(2026, 5, 10), ManausTime.localDate(instant))
        assertEquals(LocalTime.of(13, 30), ManausTime.localTime(instant))
    }

    @Test
    fun `horario perto da meia-noite cai no dia seguinte em UTC`() {
        val instant = ManausTime.toInstant(
            LocalDate.of(2026, 11, 22),
            LocalTime.of(23, 30),
        )
        assertEquals(Instant.parse("2026-11-23T03:30:00Z"), instant)
        assertEquals(LocalDate.of(2026, 11, 22), ManausTime.localDate(instant))
        assertEquals(LocalTime.of(23, 30), ManausTime.localTime(instant))
    }
}
