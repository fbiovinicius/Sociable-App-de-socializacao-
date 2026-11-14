package com.example.sociable.data.model

import com.example.sociable.core.time.ManausTime
import java.time.Instant
import java.time.ZonedDateTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventFilterTest {

    private val now = ZonedDateTime.of(2026, 5, 10, 12, 0, 0, 0, ManausTime.ZONE).toInstant()

    private fun event(
        id: String = "event-1",
        title: String = "Rolê no Centro",
        venueName: String = "Bar do Zé",
        address: String = "Rua 10 de Julho - Centro",
        category: EventCategory = EventCategory.PARTY,
        isFree: Boolean = true,
        dayOffset: Long = 0,
    ): Event {
        val start = ZonedDateTime.of(2026, 5, 10, 20, 0, 0, 0, ManausTime.ZONE)
            .plusDays(dayOffset)
            .toInstant()
        return Event(
            id = id,
            title = title,
            description = "Descrição do evento",
            category = category,
            venueName = venueName,
            address = address,
            latitude = -3.1190,
            longitude = -60.0217,
            startAt = start,
            endAt = start.plusSeconds(7200),
            isFree = isFree,
            price = if (isFree) null else 20.0,
            capacity = 40,
            imageUrl = null,
            organizerId = "user-1",
            organizerName = "Ana Souza",
            goingCount = 1,
            createdAt = now,
        )
    }

    @Test
    fun `filtro vazio aceita todos os eventos`() {
        val filter = EventFilter()
        assertTrue(filter.isEmpty)
        assertTrue(filter.matches(event(), now))
        assertTrue(filter.matches(event(dayOffset = 30), now))
    }

    @Test
    fun `busca textual ignora maiusculas e olha titulo local e endereco`() {
        assertTrue(EventFilter(query = "centro").matches(event(), now))
        assertTrue(EventFilter(query = "zé").matches(event(), now))
        assertTrue(EventFilter(query = "10 de julho").matches(event(), now))

        val filter = EventFilter(query = "praia")
        assertFalse(filter.isEmpty)
        assertFalse(filter.matches(event(), now))
    }

    @Test
    fun `filtro por categoria`() {
        assertTrue(EventFilter(categories = setOf(EventCategory.PARTY)).matches(event(), now))
        assertFalse(
            EventFilter(categories = setOf(EventCategory.TECH)).matches(event(), now)
        )
    }

    @Test
    fun `filtro somente gratis`() {
        assertTrue(EventFilter(onlyFree = true).matches(event(isFree = true), now))
        assertFalse(EventFilter(onlyFree = true).matches(event(isFree = false), now))
    }

    @Test
    fun `filtro de hoje e amanha`() {
        assertTrue(EventFilter(dateFilter = DateFilter.TODAY).matches(event(dayOffset = 0), now))
        assertFalse(EventFilter(dateFilter = DateFilter.TODAY).matches(event(dayOffset = 1), now))

        assertTrue(EventFilter(dateFilter = DateFilter.TOMORROW).matches(event(dayOffset = 1), now))
        assertFalse(EventFilter(dateFilter = DateFilter.TOMORROW).matches(event(dayOffset = 0), now))
    }

    @Test
    fun `proximos 7 dias inclui hoje e o setimo dia`() {
        val filter = EventFilter(dateFilter = DateFilter.NEXT_7_DAYS)
        assertTrue(filter.matches(event(dayOffset = 0), now))
        assertTrue(filter.matches(event(dayOffset = 7), now))
        assertFalse(filter.matches(event(dayOffset = 8), now))
        assertFalse(filter.matches(event(dayOffset = -1), now))
    }

    @Test
    fun `filtro deixa de estar vazio com qualquer criterio`() {
        assertFalse(EventFilter(query = "jazz").isEmpty)
        assertFalse(EventFilter(categories = setOf(EventCategory.MUSIC)).isEmpty)
        assertFalse(EventFilter(dateFilter = DateFilter.TODAY).isEmpty)
        assertFalse(EventFilter(onlyFree = true).isEmpty)
    }
}
