package com.example.sociable.data.firebase

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.model.Message
import com.example.sociable.data.repository.ChatRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Chat do evento em `events/{eventId}/messages`.
 *
 * Quem pode ler e escrever é decidido pelas regras do Firestore (organizador ou
 * quem confirmou presença). Sem permissão, o listener termina com erro
 * PERMISSION_DENIED, que a camada de cima trata como chat indisponível.
 *
 * Só as últimas [MESSAGE_LIMIT] mensagens são carregadas; paginação do
 * histórico fica para uma etapa posterior.
 */
class FirestoreChatRepository(
    private val firestore: FirebaseFirestore,
    private val timeProvider: TimeProvider,
) : ChatRepository {

    override fun observeMessages(eventId: String): Flow<List<Message>> = callbackFlow {
        val query = messages(eventId)
            .orderBy(MessageFields.SENT_AT, Query.Direction.ASCENDING)
            .limitToLast(MESSAGE_LIMIT)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(
                snapshot?.documents.orEmpty().mapNotNull { document ->
                    document.toDomainMessage(eventId)
                }
            )
        }

        awaitClose { registration.remove() }
    }

    override fun observeLastMessage(eventId: String): Flow<Message?> =
        messages(eventId)
            .orderBy(MessageFields.SENT_AT, Query.Direction.DESCENDING)
            .limit(1)
            .snapshotFlow()
            .map { snapshot ->
                snapshot.documents.firstNotNullOfOrNull { it.toDomainMessage(eventId) }
            }

    override suspend fun sendMessage(
        eventId: String,
        authorId: String,
        authorName: String,
        text: String,
    ): AppResult<Message> {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || trimmed.length > Validators.MESSAGE_MAX) {
            return AppResult.Failure(AppError.INVALID_INPUT)
        }

        return try {
            val reference = messages(eventId).document()
            val data = mapOf(
                MessageFields.AUTHOR_ID to authorId,
                MessageFields.AUTHOR_NAME to authorName.take(AUTHOR_NAME_MAX),
                MessageFields.TEXT to trimmed,
                MessageFields.SENT_AT to FieldValue.serverTimestamp(),
            )
            reference.set(data).await()
            AppResult.Success(
                Message(
                    id = reference.id,
                    eventId = eventId,
                    authorId = authorId,
                    authorName = authorName,
                    text = trimmed,
                    sentAt = timeProvider.now(),
                )
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Failure(exception.toFirebaseAppError(), exception)
        }
    }

    private fun messages(eventId: String) =
        firestore.collection(COLLECTION_EVENTS).document(eventId).collection(SUBCOLLECTION_MESSAGES)

    /** Mensagem recém-enviada ainda sem horário do servidor usa a estimativa local. */
    private fun DocumentSnapshot.toDomainMessage(eventId: String): Message? =
        getData(DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?.let { data -> mapToMessage(id = id, eventId = eventId, data = data) }

    private companion object {
        const val COLLECTION_EVENTS = "events"
        const val SUBCOLLECTION_MESSAGES = "messages"
        const val MESSAGE_LIMIT = 200L
        const val AUTHOR_NAME_MAX = 100
    }
}
