package com.example.sociable.data.repository

import com.example.sociable.core.AppResult
import com.example.sociable.data.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Chat por evento.
 *
 * Implementação Firebase esperada: subcoleção `events/{eventId}/messages`,
 * ordenada por `sentAt`, com `limitToLast(50)` e `addSnapshotListener`.
 * Enviar com `add()` e `FieldValue.serverTimestamp()`.
 */
interface ChatRepository {

    fun observeMessages(eventId: String): Flow<List<Message>>

    suspend fun sendMessage(
        eventId: String,
        authorId: String,
        authorName: String,
        text: String,
    ): AppResult<Message>
}
