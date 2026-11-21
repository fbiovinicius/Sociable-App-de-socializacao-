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

    /**
     * Edita um evento já publicado. Só o organizador pode; contador de presenças,
     * organizador, data de criação e a condição de "segredinho" não mudam.
     * Falha com INVALID_INPUT se o evento estiver cancelado ou se o limite de
     * pessoas ficar abaixo de quem já confirmou presença.
     */
    suspend fun updateEvent(eventId: String, draft: EventDraft): AppResult<Event>

    /** Cancela o evento (`status = CANCELLED`). Só o organizador pode. */
    suspend fun cancelEvent(eventId: String): AppResult<Unit>

    /** `going = true` confirma presença; `false` cancela. */
    suspend fun setRsvp(eventId: String, userId: String, going: Boolean): AppResult<RsvpInfo>

    fun observeRsvp(eventId: String, userId: String): Flow<RsvpInfo>
}
