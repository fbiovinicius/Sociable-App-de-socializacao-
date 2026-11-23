package com.example.sociable.data.fake

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.model.Message
import com.example.sociable.data.repository.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeChatRepository(
    private val db: FakeDatabase,
    private val timeProvider: TimeProvider,
) : ChatRepository {

    override fun observeMessages(eventId: String): Flow<List<Message>> =
        db.messages.map { all ->
            all[eventId].orEmpty().sortedBy { it.sentAt }
        }

    override fun observeLastMessage(eventId: String): Flow<Message?> =
        db.messages.map { all -> all[eventId].orEmpty().maxByOrNull { it.sentAt } }

    override suspend fun sendMessage(
        eventId: String,
        authorId: String,
        authorName: String,
        text: String,
    ): AppResult<Message> {
        delay(180)
        val trimmed = text.trim()
        if (trimmed.isEmpty() || trimmed.length > Validators.MESSAGE_MAX) {
            return AppResult.Failure(AppError.INVALID_INPUT)
        }
        val message = Message(
            id = "message-${timeProvider.now().toEpochMilli()}-${db.messages.value.size}",
            eventId = eventId,
            authorId = authorId,
            authorName = authorName,
            text = trimmed,
            sentAt = timeProvider.now(),
        )
        db.messages.update { all ->
            all + (eventId to (all[eventId].orEmpty() + message))
        }
        return AppResult.Success(message)
    }
}
