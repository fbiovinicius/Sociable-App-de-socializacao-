package com.example.sociable.data.firebase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.util.Date

class FirestoreChatMapperTest {

    private val sentAt = Instant.parse("2026-03-10T20:00:00Z")

    private fun data(): Map<String, Any?> = mapOf(
        MessageFields.AUTHOR_ID to "user-1",
        MessageFields.AUTHOR_NAME to "Ana",
        MessageFields.TEXT to "Bora?",
        MessageFields.SENT_AT to Date.from(sentAt),
    )

    @Test
    fun `mapeia documento completo`() {
        val message = mapToMessage("m1", "event-1", data())

        assertNotNull(message)
        assertEquals("m1", message!!.id)
        assertEquals("event-1", message.eventId)
        assertEquals("user-1", message.authorId)
        assertEquals("Ana", message.authorName)
        assertEquals("Bora?", message.text)
        assertEquals(sentAt, message.sentAt)
    }

    @Test
    fun `descarta documento sem texto, autor ou data`() {
        assertNull(mapToMessage("m1", "e", data() - MessageFields.TEXT))
        assertNull(mapToMessage("m1", "e", data() - MessageFields.AUTHOR_ID))
        assertNull(mapToMessage("m1", "e", data() - MessageFields.SENT_AT))
    }

    @Test
    fun `nome do autor ausente vira vazio`() {
        val message = mapToMessage("m1", "e", data() - MessageFields.AUTHOR_NAME)

        assertEquals("", message!!.authorName)
    }
}
