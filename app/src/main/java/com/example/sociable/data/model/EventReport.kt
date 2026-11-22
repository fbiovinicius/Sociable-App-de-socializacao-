package com.example.sociable.data.model

import java.time.Instant

/**
 * Motivos de denúncia de um evento.
 *
 * O texto fica aqui (como em [EventCategory]) porque é um dado curto e
 * reutilizado na tela de denúncia e no registro da denúncia.
 */
enum class ReportReason(val label: String) {
    INAPPROPRIATE_CONTENT("Conteúdo impróprio"),
    SPAM_OR_SCAM("Spam ou golpe"),
    FAKE_EVENT("Evento falso"),
    WRONG_LOCATION("Local incorreto"),
    OTHER("Outro"),
}

/** Denúncia enviada por uma pessoa sobre um evento. */
data class EventReport(
    val eventId: String,
    val reporterId: String,
    val reason: ReportReason,
    val comment: String = "",
    val createdAt: Instant,
)
