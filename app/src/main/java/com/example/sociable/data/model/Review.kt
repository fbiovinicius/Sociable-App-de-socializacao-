package com.example.sociable.data.model

import java.time.Instant

/**
 * Avaliação de um evento. Regra do MVP: uma por usuário por evento.
 * O backend deve garantir isso (id do documento = "${eventId}_${authorId}").
 */
data class Review(
    val id: String,
    val eventId: String,
    val authorId: String,
    val authorName: String,
    val rating: Int,
    val comment: String,
    val createdAt: Instant,
)
