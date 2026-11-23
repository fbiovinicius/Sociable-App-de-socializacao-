package com.example.sociable.data.fake

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
import com.example.sociable.data.repository.EventRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeEventRepository(
    private val db: FakeDatabase,
    private val timeProvider: TimeProvider,
) : EventRepository {

    override fun observeEvents(filter: EventFilter): Flow<List<Event>> =
        db.events.map { events ->
            val now = timeProvider.now()
            events
                .filter { it.status == EventStatus.ACTIVE && filter.matches(it, now) }
                .sortedWith(compareBy({ it.startAt }, { it.title }))
        }

    override fun observeChatEvents(userId: String): Flow<List<Event>> =
        combine(db.events, db.rsvps) { events, rsvps ->
            events.filter { it.organizerId == userId || userId in rsvps[it.id].orEmpty() }
        }

    override fun observeEvent(eventId: String): Flow<Event?> =
        db.events.map { events -> events.firstOrNull { it.id == eventId } }

    override suspend fun getEvent(eventId: String): AppResult<Event> {
        delay(150)
        val event = db.events.value.firstOrNull { it.id == eventId }
            ?: return AppResult.Failure(AppError.NOT_FOUND)
        return AppResult.Success(event)
    }

    override suspend fun createEvent(draft: EventDraft): AppResult<Event> {
        delay(450)
        val now = timeProvider.now()
        val event = Event(
            id = "event-${db.events.value.size + 1}-${now.toEpochMilli()}",
            title = draft.title.trim(),
            description = draft.description.trim(),
            category = draft.category,
            venueName = draft.venueName.trim(),
            address = draft.address.trim(),
            latitude = draft.latitude,
            longitude = draft.longitude,
            startAt = draft.startAt,
            endAt = draft.endAt,
            isFree = draft.isFree,
            price = if (draft.isFree) null else draft.price,
            capacity = draft.capacity,
            imageUrl = draft.imageUrl?.trim()?.takeIf { it.isNotBlank() },
            organizerId = draft.organizerId,
            organizerName = draft.organizerName,
            goingCount = 0,
            createdAt = now,
            isSecret = draft.isSecret,
        )
        db.events.update { it + event }
        return AppResult.Success(event)
    }

    override suspend fun updateEvent(eventId: String, draft: EventDraft): AppResult<Event> {
        delay(300)
        val current = db.events.value.firstOrNull { it.id == eventId }
            ?: return AppResult.Failure(AppError.NOT_FOUND)
        if (current.organizerId != draft.organizerId) {
            return AppResult.Failure(AppError.PERMISSION_DENIED)
        }
        if (current.status == EventStatus.CANCELLED) {
            return AppResult.Failure(AppError.INVALID_INPUT)
        }
        if (draft.capacity != null && draft.capacity < current.goingCount) {
            return AppResult.Failure(AppError.INVALID_INPUT)
        }
        val updated = current.copy(
            title = draft.title.trim(),
            description = draft.description.trim(),
            category = draft.category,
            venueName = draft.venueName.trim(),
            address = draft.address.trim(),
            latitude = draft.latitude,
            longitude = draft.longitude,
            startAt = draft.startAt,
            endAt = draft.endAt,
            isFree = draft.isFree,
            price = if (draft.isFree) null else draft.price,
            capacity = draft.capacity,
            imageUrl = draft.imageUrl?.trim()?.takeIf { it.isNotBlank() },
        )
        db.events.update { events -> events.map { if (it.id == eventId) updated else it } }
        return AppResult.Success(updated)
    }

    override suspend fun cancelEvent(eventId: String): AppResult<Unit> {
        delay(250)
        if (db.events.value.none { it.id == eventId }) {
            return AppResult.Failure(AppError.NOT_FOUND)
        }
        db.events.update { events ->
            events.map {
                if (it.id == eventId) it.copy(status = EventStatus.CANCELLED) else it
            }
        }
        return AppResult.Success(Unit)
    }

    override suspend fun setRsvp(
        eventId: String,
        userId: String,
        going: Boolean,
    ): AppResult<RsvpInfo> {
        delay(250)
        val event = db.events.value.firstOrNull { it.id == eventId }
            ?: return AppResult.Failure(AppError.NOT_FOUND)

        val current = db.rsvps.value[eventId].orEmpty()
        val alreadyGoing = userId in current

        if (going == alreadyGoing) {
            return AppResult.Success(
                RsvpInfo(goingCount = current.size, isGoing = alreadyGoing, capacity = event.capacity)
            )
        }
        if (going && event.capacity != null && current.size >= event.capacity) {
            return AppResult.Failure(AppError.EVENT_FULL)
        }

        val updated = if (going) current + userId else current - userId
        db.rsvps.update { it + (eventId to updated) }
        db.events.update { events ->
            events.map {
                if (it.id == eventId) it.copy(goingCount = updated.size) else it
            }
        }
        return AppResult.Success(
            RsvpInfo(goingCount = updated.size, isGoing = going, capacity = event.capacity)
        )
    }

    override fun observeRsvp(eventId: String, userId: String): Flow<RsvpInfo> =
        combine(db.events, db.rsvps) { events, rsvps ->
            val capacity = events.firstOrNull { it.id == eventId }?.capacity
            val going = rsvps[eventId].orEmpty()
            RsvpInfo(
                goingCount = going.size,
                isGoing = userId in going,
                capacity = capacity,
            )
        }

    override fun observeAttendees(eventId: String): Flow<List<Attendee>> =
        combine(db.rsvps, db.users) { rsvps, users ->
            rsvps[eventId].orEmpty().mapNotNull { userId ->
                users.firstOrNull { it.id == userId }?.let { user ->
                    Attendee(userId = user.id, name = user.name, photoUrl = user.photoUrl)
                }
            }
        }
}
