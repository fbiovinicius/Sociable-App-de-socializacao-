package com.example.sociable.core.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderPolicyTest {

    private val now = 1_700_000_000_000L

    @Test
    fun `evento amanha dispara 1 hora antes`() {
        val startAt = now + 24 * 60 * ReminderPolicy.MILLIS_PER_MINUTE

        val delay = ReminderPolicy.delayMillis(
            startAtMillis = startAt,
            nowMillis = now,
        )

        assertEquals(
            23 * 60 * ReminderPolicy.MILLIS_PER_MINUTE,
            delay,
        )
    }

    @Test
    fun `lead customizado muda o momento do aviso`() {
        val startAt = now + 90 * ReminderPolicy.MILLIS_PER_MINUTE

        val delay = ReminderPolicy.delayMillis(
            startAtMillis = startAt,
            nowMillis = now,
            leadMinutes = 30L,
        )

        assertEquals(
            60 * ReminderPolicy.MILLIS_PER_MINUTE,
            delay,
        )
    }

    @Test
    fun `evento comeca antes do intervalo nao tem lembrete`() {
        val startAt = now + 30 * ReminderPolicy.MILLIS_PER_MINUTE

        val delay = ReminderPolicy.delayMillis(
            startAtMillis = startAt,
            nowMillis = now,
            leadMinutes = 60L,
        )

        assertNull(delay)
    }

    @Test
    fun `evento que ja comecou nao tem lembrete`() {
        val startAt = now - ReminderPolicy.MILLIS_PER_MINUTE

        val delay = ReminderPolicy.delayMillis(
            startAtMillis = startAt,
            nowMillis = now,
        )

        assertNull(delay)
    }

    @Test
    fun `momento exato do aviso nao agenda`() {
        val startAt = now + 60 * ReminderPolicy.MILLIS_PER_MINUTE

        val delay = ReminderPolicy.delayMillis(
            startAtMillis = startAt,
            nowMillis = now,
            leadMinutes = 60L,
        )

        assertNull(delay)
    }
}
