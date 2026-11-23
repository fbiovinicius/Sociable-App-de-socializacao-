package com.example.sociable.data.repository

import com.example.sociable.core.AppResult
import com.example.sociable.data.model.Attendee
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
 *  - setRsvp         -> subcoleção `events/{eventId}/rsvps/{userId}`, o documento
 *                       público em `attendees/{userId}` e `goingCount` são
 *                       gravados na mesma transação (para respeitar `capacity`).
 *  - observeAttendees-> subcoleção pública `events/{eventId}/attendees/{userId}`
 *                       com nome e foto de quem confirmou presença (prévia do mapa).
 */
interface EventRepository {

    fun observeEvents(filter: EventFilter): Flow<List<Event>>

    fun observeEvent(eventId: String): Flow<Event?>

    /**
     * Eventos que têm chat para a pessoa: os que ela organiza e aqueles em que
     * confirmou presença (inclui cancelados). Sem ordem definida.
     */
    fun observeChatEvents(userId: String): Flow<List<Event>>

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

    /**
     * Quem confirmou presença no evento (prévia com nome e foto de perfil).
     * A lista é curta por definição: serve para mostrar as primeiras pessoas.
     */
    fun observeAttendees(eventId: String): Flow<List<Attendee>>
}
