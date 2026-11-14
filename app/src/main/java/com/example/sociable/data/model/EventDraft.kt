package com.example.sociable.data.model

import java.time.Instant

/**
 * Dados de entrada para criar um evento.
 * Separado de [Event] porque o id, contador e createdAt são gerados pela camada de dados.
 */
data class EventDraft(
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
    val price: Double?,
    val capacity: Int?,
    val imageUrl: String?,
    val organizerId: String,
    val organizerName: String,
)
