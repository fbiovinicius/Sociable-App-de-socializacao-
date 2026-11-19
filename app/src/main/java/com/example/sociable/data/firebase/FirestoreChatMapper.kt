package com.example.sociable.data.firebase

import com.example.sociable.data.model.Message

/** Nomes dos campos da subcoleção `events/{eventId}/messages`. */
internal object MessageFields {
    const val AUTHOR_ID = "authorId"
    const val AUTHOR_NAME = "authorName"
    const val TEXT = "text"
    const val SENT_AT = "sentAt"
}

/**
 * Conversão pura de um documento de mensagem para o modelo de domínio.
 *
 * Devolve `null` para documentos incompletos, para que um documento malformado
 * não derrube a conversa inteira. `sentAt` precisa vir como Timestamp: para
 * mensagens ainda não confirmadas pelo servidor, quem lê o snapshot usa
 * `ServerTimestampBehavior.ESTIMATE` e o campo chega preenchido.
 */
internal fun mapToMessage(id: String, eventId: String, data: Map<String, Any?>): Message? {
    val authorId = data[MessageFields.AUTHOR_ID] as? String ?: return null
    val text = data[MessageFields.TEXT] as? String ?: return null
    val sentAt = data[MessageFields.SENT_AT].toInstantOrNull() ?: return null

    return Message(
        id = id,
        eventId = eventId,
        authorId = authorId,
        authorName = data[MessageFields.AUTHOR_NAME] as? String ?: "",
        text = text,
        sentAt = sentAt,
    )
}
