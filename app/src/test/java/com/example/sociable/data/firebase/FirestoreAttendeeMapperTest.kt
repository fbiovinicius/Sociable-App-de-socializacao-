package com.example.sociable.data.firebase

import com.example.sociable.data.model.Attendee
import com.example.sociable.data.model.ReportReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreAttendeeMapperTest {

    @Test
    fun `documento de participante guarda apenas o necessario`() {
        val attendee = Attendee(
            userId = "user-1",
            name = "Ana Souza",
            photoUrl = "https://exemplo.com/ana.png",
        )

        val map = attendee.toAttendeeMap(createdAt = "agora")

        assertEquals("user-1", map[AttendeeFields.USER_ID])
        assertEquals("Ana Souza", map[AttendeeFields.NAME])
        assertEquals("https://exemplo.com/ana.png", map[AttendeeFields.PHOTO_URL])
        assertEquals("agora", map[AttendeeFields.CREATED_AT])
        assertEquals(setOf("userId", "name", "photoUrl", "createdAt"), map.keys)
    }

    @Test
    fun `participante sem foto mantem o campo nulo`() {
        val attendee = Attendee(userId = "user-2", name = "Bruno Lima")

        val map = attendee.toAttendeeMap(createdAt = "agora")

        assertEquals(null, map[AttendeeFields.PHOTO_URL])
        assertTrue(map.containsKey(AttendeeFields.PHOTO_URL))
    }

    @Test
    fun `motivos de denuncia tem rotulos em portugues`() {
        assertEquals(
            listOf(
                "Conteúdo impróprio",
                "Spam ou golpe",
                "Evento falso",
                "Local incorreto",
                "Outro",
            ),
            ReportReason.entries.map { it.label },
        )
    }
}
