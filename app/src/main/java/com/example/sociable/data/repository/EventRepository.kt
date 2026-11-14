package com.example.sociable.data.repository

import com.example.sociable.core.AppResult
import com.example.sociable.data.model.Event
import com.example.sociable.data.model.EventDraft
import com.example.sociable.data.model.EventFilter
import com.example.sociable.data.model.RsvpInfo
import kotlinx.coroutines.flow.Flow

/**
 * Feed, detalhe, criação e presença.
 *
 * Implementação Firebase esperada:
 *  - observeEvents   -> collection `events` + filtros; listener em tempo real.
 *                       Para "perto de mim", usar geohash (geofire-common).
 *  - observeEvent    -> doc `events/{eventId}`.
 *  - setRsvp         -> subcoleção `events/{eventId}/rsvps/{userId}` e
 *                       `FieldValue.increment(±1)` em `events/{eventId}.goingCount`
 *                       dentro de uma transação (para respeitar `capacity`).
 */
interface EventRepository {

    fun observeEvents(filter: EventFilter): Flow<List<Event>>

    fun observeEvent(eventId: String): Flow<Event?>

    suspend fun getEvent(eventId: String): AppResult<Event>

    suspend fun createEvent(draft: EventDraft): AppResult<Event>

    /** `going = true` confirma presença; `false` cancela. */
    suspend fun setRsvp(eventId: String, userId: String, going: Boolean): AppResult<RsvpInfo>

    fun observeRsvp(eventId: String, userId: String): Flow<RsvpInfo>
}
