package com.example.sociable.data.model

/**
 * Situação do evento no backend.
 *
 * Eventos cancelados continuam existindo no Firestore para que o organizador e
 * quem confirmou presença consigam abrir o detalhe e ver o aviso, mas somem do
 * feed e do mapa.
 */
enum class EventStatus {
    ACTIVE,
    CANCELLED,
}
