package com.example.sociable.data.repository

import com.example.sociable.core.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * Eventos que a pessoa salvou (favoritos) para não esquecer.
 *
 * Implementação Firebase: `users/{userId}/savedEvents/{eventId}`. O lembrete local
 * de um evento salvo é agendado por quem chama (ver `EventDetailViewModel`).
 */
interface SavedEventRepository {

    /** Ids dos eventos salvos pela pessoa. */
    fun observeSavedIds(userId: String): Flow<Set<String>>

    suspend fun setSaved(userId: String, eventId: String, saved: Boolean): AppResult<Unit>
}
