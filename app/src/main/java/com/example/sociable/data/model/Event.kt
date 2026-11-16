package com.example.sociable.data.model

import java.time.Instant

/**
 * Evento exibido no feed, no mapa e no detalhe.
 *
 * `latitude`/`longitude` são campos simples; o backend pode manter também um
 * `geohash` derivado deles para consultas por proximidade no Firestore.
 * `goingCount` é desnormalizado (contador) para o feed não precisar de N leituras.
 */
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val category: EventCategory,
    val venueName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val startAt: Instant,
    val endAt: Instant,
    val isFree: Boolean,
    val price: Double? = null,
    val capacity: Int? = null,
    val imageUrl: String? = null,
    val organizerId: String,
    val organizerName: String,
    val goingCount: Int = 0,
    val createdAt: Instant,
    /**
     * "Segredinho": evento com local divulgado apenas para quem confirmou presença.
     * O backend deve ocultar `venueName`/`address` de quem não tem RSVP.
     */
    val isSecret: Boolean = false,
    /** Cancelado some do feed/mapa, mas continua acessível por quem já conhecia o evento. */
    val status: EventStatus = EventStatus.ACTIVE,
) {
    fun isHappeningAt(now: Instant): Boolean =
        !now.isBefore(startAt) && !now.isAfter(endAt)

    fun isFinishedAt(now: Instant): Boolean = now.isAfter(endAt)
}
