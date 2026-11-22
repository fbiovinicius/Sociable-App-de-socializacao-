@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.sociable.data.firebase

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.data.model.Attendee
import com.example.sociable.data.model.Event
import com.example.sociable.data.model.EventDraft
import com.example.sociable.data.model.EventFilter
import com.example.sociable.data.model.EventStatus
import com.example.sociable.data.model.RsvpInfo
import com.example.sociable.data.model.matches
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.EventRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

/**
 * Eventos, presença e local privado no Cloud Firestore.
 *
 * Decisões de MVP:
 *  - o servidor busca os eventos ativos que ainda não terminaram (até [FEED_LIMIT])
 *    e os filtros de texto/categoria são aplicados em memória com [matches];
 *    paginação, busca full-text e geohash ficam para uma etapa posterior;
 *  - `goingCount` é atualizado em transação junto com o documento de RSVP;
 *  - cada RSVP também escreve (ou apaga) o documento público em
 *    `events/{id}/attendees/{userId}`, que alimenta a prévia de participantes
 *    mostrada no mapa;
 *  - o local de um segredinho fica em `events/{id}/private/location` e só é
 *    mesclado ao modelo quando o usuário é o organizador ou já confirmou presença.
 */
class FirestoreEventRepository(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val timeProvider: TimeProvider,
) : EventRepository {

    private val events: CollectionReference
        get() = firestore.collection(COLLECTION_EVENTS)

    private val users: CollectionReference
        get() = firestore.collection(COLLECTION_USERS)

    override fun observeEvents(filter: EventFilter): Flow<List<Event>> = callbackFlow {
        val query = events
            .whereEqualTo(EventFields.STATUS, EventStatus.ACTIVE.name)
            .whereGreaterThanOrEqualTo(EventFields.END_AT, Timestamp(Date.from(timeProvider.now())))
            .orderBy(EventFields.END_AT, Query.Direction.ASCENDING)
            .limit(FEED_LIMIT)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val now = timeProvider.now()
            val result = snapshot?.documents.orEmpty()
                .mapNotNull { document -> document.toDomainEvent() }
                .filter { event -> filter.matches(event, now) }
                .sortedWith(compareBy({ it.startAt }, { it.title }))
            trySend(result)
        }

        awaitClose { registration.remove() }
    }

    override fun observeEvent(eventId: String): Flow<Event?> =
        documentFlow(events.document(eventId))
            .map { snapshot -> snapshot.toDomainEvent() }
            .flatMapLatest { event ->
                flow { emit(event?.let { resolveSecretLocation(it) }) }
            }

    override suspend fun getEvent(eventId: String): AppResult<Event> = try {
        val snapshot = events.document(eventId).get().await()
        val event = snapshot.toDomainEvent()
            ?: return AppResult.Failure(AppError.NOT_FOUND)
        AppResult.Success(resolveSecretLocation(event))
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        AppResult.Failure(exception.toFirebaseAppError(), exception)
    }

    override suspend fun createEvent(draft: EventDraft): AppResult<Event> = try {
        val reference = events.document()
        val event = draft.toPublicEvent(id = reference.id, createdAt = timeProvider.now())
        val batch = firestore.batch()
        batch.set(reference, event.toFirestoreMap())
        if (draft.isSecret) {
            batch.set(
                reference.collection(SUBCOLLECTION_PRIVATE).document(PRIVATE_LOCATION_DOCUMENT),
                draft.toPrivateLocationMap(),
            )
        }
        batch.commit().await()
        AppResult.Success(event)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        AppResult.Failure(exception.toFirebaseAppError(), exception)
    }

    override suspend fun setRsvp(
        eventId: String,
        userId: String,
        going: Boolean,
    ): AppResult<RsvpInfo> = try {
        val outcome = firestore.runTransaction { transaction ->
            val eventReference = events.document(eventId)
            val rsvpReference = eventReference.collection(SUBCOLLECTION_RSVPS).document(userId)
            val attendeeReference = eventReference
                .collection(SUBCOLLECTION_ATTENDEES)
                .document(userId)

            // Todas as leituras precisam acontecer antes de qualquer escrita em
            // uma transação do Firestore.
            val eventSnapshot = transaction.get(eventReference)
            if (!eventSnapshot.exists()) {
                return@runTransaction RsvpTransactionResult.Missing
            }

            val alreadyGoing = transaction.get(rsvpReference).exists()
            val attendeeExists = transaction.get(attendeeReference).exists()
            val goingCount = eventSnapshot.getLong(EventFields.GOING_COUNT)?.toInt() ?: 0
            val capacity = eventSnapshot.getLong(EventFields.CAPACITY)?.toInt()
            val attendeeMap = if (going) {
                val profileSnapshot = transaction.get(users.document(userId))
                val attendee = Attendee(
                    userId = userId,
                    name = profileSnapshot.getString(AttendeeFields.NAME).orEmpty(),
                    photoUrl = profileSnapshot.getString(AttendeeFields.PHOTO_URL),
                )
                attendee.toAttendeeMap(FieldValue.serverTimestamp())
            } else {
                emptyMap()
            }

            when {
                going == alreadyGoing -> {
                    // Recupera uma prévia ausente ou remove um documento antigo
                    // sem alterar o RSVP e o contador já consistentes.
                    if (going) {
                        if (attendeeExists) {
                            transaction.update(
                                attendeeReference,
                                AttendeeFields.NAME,
                                attendeeMap[AttendeeFields.NAME],
                                AttendeeFields.PHOTO_URL,
                                attendeeMap[AttendeeFields.PHOTO_URL],
                            )
                        } else {
                            transaction.set(attendeeReference, attendeeMap)
                        }
                    } else if (attendeeExists) {
                        transaction.delete(attendeeReference)
                    }
                    RsvpTransactionResult.Done(
                        RsvpInfo(goingCount = goingCount, isGoing = alreadyGoing, capacity = capacity)
                    )
                }

                going && capacity != null && goingCount >= capacity ->
                    RsvpTransactionResult.Full

                going -> {
                    transaction.set(rsvpReference, newRsvpDocument(userId))
                    transaction.update(eventReference, EventFields.GOING_COUNT, goingCount + 1)
                    if (attendeeExists) {
                        transaction.update(
                            attendeeReference,
                            AttendeeFields.NAME,
                            attendeeMap[AttendeeFields.NAME],
                            AttendeeFields.PHOTO_URL,
                            attendeeMap[AttendeeFields.PHOTO_URL],
                        )
                    } else {
                        transaction.set(attendeeReference, attendeeMap)
                    }
                    RsvpTransactionResult.Done(
                        RsvpInfo(goingCount = goingCount + 1, isGoing = true, capacity = capacity)
                    )
                }

                else -> {
                    transaction.delete(rsvpReference)
                    val nextCount = (goingCount - 1).coerceAtLeast(0)
                    transaction.update(eventReference, EventFields.GOING_COUNT, nextCount)
                    if (attendeeExists) transaction.delete(attendeeReference)
                    RsvpTransactionResult.Done(
                        RsvpInfo(goingCount = nextCount, isGoing = false, capacity = capacity)
                    )
                }
            }
        }.await()

        when (outcome) {
            is RsvpTransactionResult.Done -> AppResult.Success(outcome.info)
            RsvpTransactionResult.Full -> AppResult.Failure(AppError.EVENT_FULL)
            RsvpTransactionResult.Missing -> AppResult.Failure(AppError.NOT_FOUND)
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        AppResult.Failure(exception.toFirebaseAppError(), exception)
    }

    override fun observeRsvp(eventId: String, userId: String): Flow<RsvpInfo> {
        val eventReference = events.document(eventId)
        val rsvpReference = eventReference.collection(SUBCOLLECTION_RSVPS).document(userId)
        return combine(
            documentFlow(eventReference),
            documentFlow(rsvpReference),
        ) { eventSnapshot, rsvpSnapshot ->
            val data = eventSnapshot.data.orEmpty()
            RsvpInfo(
                goingCount = (data[EventFields.GOING_COUNT] as? Number)?.toInt() ?: 0,
                isGoing = rsvpSnapshot.exists(),
                capacity = (data[EventFields.CAPACITY] as? Number)?.toInt(),
            )
        }
    }

    /**
     * Prévia de quem confirmou presença: documentos públicos em
     * `events/{id}/attendees`, em ordem de chegada (mais recentes no fim).
     */
    override fun observeAttendees(eventId: String): Flow<List<Attendee>> = callbackFlow {
        val registration = events.document(eventId)
            .collection(SUBCOLLECTION_ATTENDEES)
            .orderBy(AttendeeFields.CREATED_AT, Query.Direction.ASCENDING)
            .limit(ATTENDEE_LIMIT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val attendees = snapshot?.documents.orEmpty().mapNotNull { it.toDomainAttendee() }
                trySend(attendees)
            }

        awaitClose { registration.remove() }
    }

    /**
     * Mescla o local real apenas para organizador ou participante. A leitura do
     * documento privado é protegida pelas regras do Firestore; se falhar, o
     * evento continua com os valores públicos.
     */
    private suspend fun resolveSecretLocation(event: Event): Event {
        if (!event.isSecret) return event

        val userId = authRepository.currentUser.value?.id ?: return event
        if (event.organizerId != userId) {
            val rsvp = events.document(event.id)
                .collection(SUBCOLLECTION_RSVPS)
                .document(userId)
                .get()
                .await()
            if (!rsvp.exists()) return event
        }

        return try {
            val location = events.document(event.id)
                .collection(SUBCOLLECTION_PRIVATE)
                .document(PRIVATE_LOCATION_DOCUMENT)
                .get()
                .await()
            if (!location.exists()) {
                event
            } else {
                event.copy(
                    venueName = location.getString(EventFields.VENUE_NAME) ?: event.venueName,
                    address = location.getString(EventFields.ADDRESS) ?: event.address,
                    latitude = location.getDouble(EventFields.LATITUDE) ?: event.latitude,
                    longitude = location.getDouble(EventFields.LONGITUDE) ?: event.longitude,
                )
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            // Sem permissão para o local privado: mantém a versão pública.
            event
        }
    }

    private fun documentFlow(reference: DocumentReference): Flow<DocumentSnapshot> = callbackFlow {
        val registration = reference.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot)
            }
        }
        awaitClose { registration.remove() }
    }

    private fun newRsvpDocument(userId: String): Map<String, Any?> = mapOf(
        FIELD_USER_ID to userId,
        FIELD_CREATED_AT to Timestamp(Date.from(timeProvider.now())),
        FIELD_CHECKED_IN to false,
        FIELD_CHECK_IN_TOKEN to UUID.randomUUID().toString(),
    )

    private sealed interface RsvpTransactionResult {
        data class Done(val info: RsvpInfo) : RsvpTransactionResult
        data object Full : RsvpTransactionResult
        data object Missing : RsvpTransactionResult
    }

    private companion object {
        const val COLLECTION_EVENTS = "events"
        const val COLLECTION_USERS = "users"
        const val SUBCOLLECTION_RSVPS = "rsvps"
        const val SUBCOLLECTION_ATTENDEES = "attendees"
        const val SUBCOLLECTION_PRIVATE = "private"
        const val PRIVATE_LOCATION_DOCUMENT = "location"
        const val FEED_LIMIT = 200L
        const val ATTENDEE_LIMIT = 30L

        const val FIELD_USER_ID = "userId"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_CHECKED_IN = "checkedIn"
        const val FIELD_CHECK_IN_TOKEN = "checkInToken"
    }
}
