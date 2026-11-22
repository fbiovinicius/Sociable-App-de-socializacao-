package com.example.sociable.data.firebase

import com.example.sociable.data.model.Event
import com.example.sociable.data.model.EventCategory
import com.example.sociable.data.model.EventDraft
import com.example.sociable.data.model.EventStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class FirestoreEventMapperTest {

    private val startAt = Instant.parse("2026-03-10T20:00:00.123456Z")
    private val endAt = Instant.parse("2026-03-11T00:00:00.654321Z")
    private val createdAt = Instant.parse("2026-03-01T12:00:00.987654Z")

    private val event = Event(
        id = "event-1",
        title = "Rolê no Centro",
        description = "Música ao vivo, petiscos e gente nova.",
        category = EventCategory.PARTY,
        venueName = "Bar do Zé",
        address = "Rua 10 de Julho, 123 - Centro",
        latitude = -3.1341,
        longitude = -60.0234,
        startAt = startAt,
        endAt = endAt,
        isFree = false,
        price = 20.0,
        capacity = 40,
        imageUrl = "https://example.com/foto.jpg",
        organizerId = "user-1",
        organizerName = "Ana Souza",
        goingCount = 2,
        createdAt = createdAt,
        isSecret = false,
    )

    @Test
    fun `mapa do firestore faz roundtrip do evento`() {
        val restored = mapToEvent(event.id, event.toFirestoreMap())

        assertEquals(event, restored)
    }

    @Test
    fun `evento secreto esconde o local no documento publico`() {
        val draft = draft(isSecret = true)

        val publicEvent = draft.toPublicEvent(id = "event-9", createdAt = createdAt)

        assertEquals(SECRET_VENUE_PLACEHOLDER, publicEvent.venueName)
        assertEquals(SECRET_ADDRESS_PLACEHOLDER, publicEvent.address)
        assertNotEquals(draft.venueName, publicEvent.venueName)
        assertNotEquals(draft.address, publicEvent.address)
        assertNotEquals(draft.latitude, publicEvent.latitude)
        assertNotEquals(draft.longitude, publicEvent.longitude)
    }

    @Test
    fun `documento privado guarda o local real do segredinho`() {
        val draft = draft(isSecret = true)

        val privateMap = draft.toPrivateLocationMap()

        assertEquals(draft.venueName.trim(), privateMap[EventFields.VENUE_NAME])
        assertEquals(draft.address.trim(), privateMap[EventFields.ADDRESS])
        assertEquals(draft.latitude, privateMap[EventFields.LATITUDE] as Double, 0.0)
        assertEquals(draft.longitude, privateMap[EventFields.LONGITUDE] as Double, 0.0)
    }

    @Test
    fun `evento publico mantem o local real`() {
        val draft = draft(isSecret = false)

        val publicEvent = draft.toPublicEvent(id = "event-9", createdAt = createdAt)

        assertEquals(draft.venueName.trim(), publicEvent.venueName)
        assertEquals(draft.address.trim(), publicEvent.address)
        assertEquals(draft.latitude, publicEvent.latitude, 0.0)
        assertEquals(draft.longitude, publicEvent.longitude, 0.0)
        assertEquals(0, publicEvent.goingCount)
        assertEquals(EventStatus.ACTIVE, publicEvent.status)
    }

    @Test
    fun `categoria e status desconhecidos caem no padrao`() {
        val data = event.toFirestoreMap() + mapOf(
            EventFields.CATEGORY to "CATEGORIA_QUE_NAO_EXISTE",
            EventFields.STATUS to "STATUS_QUE_NAO_EXISTE",
        )

        val restored = mapToEvent(event.id, data)

        assertEquals(EventCategory.OTHER, restored?.category)
        assertEquals(EventStatus.ACTIVE, restored?.status)
    }

    @Test
    fun `documento sem campos obrigatorios e ignorado`() {
        assertNull(mapToEvent("event-x", emptyMap()))
    }

    private fun draft(isSecret: Boolean) = EventDraft(
        title = "  Luau na beira do rio  ",
        description = "  Violão, fogueira e gente nova.  ",
        category = EventCategory.MUSIC,
        venueName = "  Praia da Ponta Negra  ",
        address = "  Av. Coronel Teixeira, s/n  ",
        latitude = -3.1000,
        longitude = -60.0800,
        startAt = startAt,
        endAt = endAt,
        isFree = false,
        price = 20.0,
        capacity = 8,
        imageUrl = "  https://example.com/luau.jpg  ",
        organizerId = "user-3",
        organizerName = "Carla Reis",
        isSecret = isSecret,
    )

    @Test
    fun `campos editaveis nao incluem contador, organizador nem status`() {
        val keys = event.toEditableFieldsMap(includeLocation = true).keys

        assertTrue(EventFields.TITLE in keys)
        assertTrue(EventFields.LATITUDE in keys)
        assertFalse(EventFields.GOING_COUNT in keys)
        assertFalse(EventFields.ORGANIZER_ID in keys)
        assertFalse(EventFields.CREATED_AT in keys)
        assertFalse(EventFields.STATUS in keys)
        assertFalse(EventFields.IS_SECRET in keys)
    }

    @Test
    fun `em segredinho a edicao nao envia o local para o documento publico`() {
        val keys = event.toEditableFieldsMap(includeLocation = false).keys

        assertFalse(EventFields.VENUE_NAME in keys)
        assertFalse(EventFields.ADDRESS in keys)
        assertFalse(EventFields.LATITUDE in keys)
        assertFalse(EventFields.LONGITUDE in keys)
        assertTrue(EventFields.TITLE in keys)
    }
}
