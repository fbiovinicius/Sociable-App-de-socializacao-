package com.example.sociable.data.model

import java.time.Instant

/** Mensagem do chat de um evento. */
data class Message(
    val id: String,
    val eventId: String,
    val authorId: String,
    val authorName: String,
    val text: String,
    val sentAt: Instant,
)
