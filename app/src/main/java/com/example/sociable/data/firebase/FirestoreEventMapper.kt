package com.example.sociable.data.firebase

import com.example.sociable.data.model.Event
import com.example.sociable.data.model.EventCategory
import com.example.sociable.data.model.EventDraft
import com.example.sociable.data.model.EventStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant
import java.util.Date

/** Nomes dos campos usados na coleção `events`. */
internal object EventFields {
    const val TITLE = "title"
    const val DESCRIPTION = "description"
    const val CATEGORY = "category"
    const val VENUE_NAME = "venueName"
    const val ADDRESS = "address"
    const val LATITUDE = "latitude"
    const val LONGITUDE = "longitude"
    const val START_AT = "startAt"
    const val END_AT = "endAt"
    const val IS_FREE = "isFree"
    const val PRICE = "price"
    const val CAPACITY = "capacity"
    const val IMAGE_URL = "imageUrl"
    const val ORGANIZER_ID = "organizerId"
    const val ORGANIZER_NAME = "organizerName"
    const val GOING_COUNT = "goingCount"
    const val CREATED_AT = "createdAt"
    const val IS_SECRET = "isSecret"
    const val STATUS = "status"
}

/** Valores públicos de um segredinho. O endereço real fica em `private/location`. */
internal const val SECRET_VENUE_PLACEHOLDER = "Local secreto"
internal const val SECRET_ADDRESS_PLACEHOLDER = "Revelado após confirmar presença"
private const val SECRET_LATITUDE = -3.1190
private const val SECRET_LONGITUDE = -60.0217

internal fun Event.toFirestoreMap(): Map<String, Any?> = mapOf(
    EventFields.TITLE to title,
    EventFields.DESCRIPTION to description,
    EventFields.CATEGORY to category.name,
    EventFields.VENUE_NAME to venueName,
    EventFields.ADDRESS to address,
    EventFields.LATITUDE to latitude,
    EventFields.LONGITUDE to longitude,
    EventFields.START_AT to startAt.toTimestamp(),
    EventFields.END_AT to endAt.toTimestamp(),
    EventFields.IS_FREE to isFree,
    EventFields.PRICE to price,
    EventFields.CAPACITY to capacity,
    EventFields.IMAGE_URL to imageUrl,
    EventFields.ORGANIZER_ID to organizerId,
    EventFields.ORGANIZER_NAME to organizerName,
    EventFields.GOING_COUNT to goingCount,
    EventFields.CREATED_AT to createdAt.toTimestamp(),
    EventFields.IS_SECRET to isSecret,
    EventFields.STATUS to status.name,
)

/** Converte o documento do Firestore no modelo de domínio. */
internal fun DocumentSnapshot.toDomainEvent(): Event? {
    if (!exists()) return null
    return mapToEvent(id = id, data = data.orEmpty())
}

/**
 * Conversão pura, usada nos testes. Campos opcionais recebem valores neutros
 * para que documentos antigos ou incompletos não derrubem o feed.
 */
internal fun mapToEvent(id: String, data: Map<String, Any?>): Event? {
    val title = data[EventFields.TITLE] as? String ?: return null
    val startAt = data[EventFields.START_AT].toInstantOrNull() ?: return null
    val endAt = data[EventFields.END_AT].toInstantOrNull() ?: return null
    val organizerId = data[EventFields.ORGANIZER_ID] as? String ?: return null

    return Event(
        id = id,
        title = title,
        description = data[EventFields.DESCRIPTION] as? String ?: "",
        category = (data[EventFields.CATEGORY] as? String).toCategory(),
        venueName = data[EventFields.VENUE_NAME] as? String ?: "",
        address = data[EventFields.ADDRESS] as? String ?: "",
        latitude = (data[EventFields.LATITUDE] as? Number)?.toDouble() ?: 0.0,
        longitude = (data[EventFields.LONGITUDE] as? Number)?.toDouble() ?: 0.0,
        startAt = startAt,
        endAt = endAt,
        isFree = data[EventFields.IS_FREE] as? Boolean ?: true,
        price = (data[EventFields.PRICE] as? Number)?.toDouble(),
        capacity = (data[EventFields.CAPACITY] as? Number)?.toInt(),
        imageUrl = data[EventFields.IMAGE_URL] as? String,
        organizerId = organizerId,
        organizerName = data[EventFields.ORGANIZER_NAME] as? String ?: "",
        goingCount = (data[EventFields.GOING_COUNT] as? Number)?.toInt() ?: 0,
        createdAt = data[EventFields.CREATED_AT].toInstantOrNull() ?: startAt,
        isSecret = data[EventFields.IS_SECRET] as? Boolean ?: false,
        status = (data[EventFields.STATUS] as? String).toStatus(),
    )
}

/**
 * Monta o evento que vai para o documento público.
 *
 * Em um segredinho, nome do local, endereço e coordenadas são substituídos por
 * valores genéricos. O local real só é gravado em `events/{id}/private/location`.
 */
internal fun EventDraft.toPublicEvent(id: String, createdAt: Instant): Event = Event(
    id = id,
    title = title.trim(),
    description = description.trim(),
    category = category,
    venueName = if (isSecret) SECRET_VENUE_PLACEHOLDER else venueName.trim(),
    address = if (isSecret) SECRET_ADDRESS_PLACEHOLDER else address.trim(),
    latitude = if (isSecret) SECRET_LATITUDE else latitude,
    longitude = if (isSecret) SECRET_LONGITUDE else longitude,
    startAt = startAt,
    endAt = endAt,
    isFree = isFree,
    price = if (isFree) null else price,
    capacity = capacity,
    imageUrl = imageUrl?.trim()?.takeIf { it.isNotBlank() },
    organizerId = organizerId,
    organizerName = organizerName,
    goingCount = 0,
    createdAt = createdAt,
    isSecret = isSecret,
    status = EventStatus.ACTIVE,
)

/** Documento privado com o local real, legível apenas por participantes. */
internal fun EventDraft.toPrivateLocationMap(): Map<String, Any?> = mapOf(
    EventFields.VENUE_NAME to venueName.trim(),
    EventFields.ADDRESS to address.trim(),
    EventFields.LATITUDE to latitude,
    EventFields.LONGITUDE to longitude,
)

internal fun Any?.toInstantOrNull(): Instant? = when (this) {
    is Timestamp -> Instant.ofEpochSecond(seconds, nanoseconds.toLong())
    is Date -> toInstant()
    is Number -> Instant.ofEpochMilli(toLong())
    else -> null
}

internal fun Instant.toTimestamp(): Timestamp = Timestamp(epochSecond, nano)

private fun String?.toCategory(): EventCategory =
    this?.let { name ->
        runCatching { EventCategory.valueOf(name) }.getOrNull()
    } ?: EventCategory.OTHER

private fun String?.toStatus(): EventStatus =
    this?.let { name ->
        runCatching { EventStatus.valueOf(name) }.getOrNull()
    } ?: EventStatus.ACTIVE
