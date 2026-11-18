package com.example.sociable.data.firebase

import com.example.sociable.data.model.RatingSummary
import com.example.sociable.data.model.Review

/** Nomes dos campos da subcoleção `events/{eventId}/reviews`. */
internal object ReviewFields {
    const val AUTHOR_ID = "authorId"
    const val AUTHOR_NAME = "authorName"
    const val RATING = "rating"
    const val COMMENT = "comment"
    const val CREATED_AT = "createdAt"
}

/**
 * Id de domínio da avaliação, igual ao usado pelos repositórios fake.
 * No Firestore, o documento fica em `events/{eventId}/reviews/{authorId}`:
 * o id do documento ser o autor é o que garante uma avaliação por pessoa.
 */
internal fun reviewId(eventId: String, authorId: String): String = "${eventId}_$authorId"

/**
 * Conversão pura de um documento de avaliação para o modelo de domínio.
 *
 * [documentId] é o id do documento, que é o uid do autor. Devolve `null` quando
 * faltam a nota ou a data, ou quando a nota está fora do intervalo de 1 a 5.
 */
internal fun mapToReview(eventId: String, documentId: String, data: Map<String, Any?>): Review? {
    val rating = (data[ReviewFields.RATING] as? Number)?.toInt() ?: return null
    if (rating !in 1..5) return null
    val createdAt = data[ReviewFields.CREATED_AT].toInstantOrNull() ?: return null
    val authorId = data[ReviewFields.AUTHOR_ID] as? String ?: documentId

    return Review(
        id = reviewId(eventId, authorId),
        eventId = eventId,
        authorId = authorId,
        authorName = data[ReviewFields.AUTHOR_NAME] as? String ?: "",
        rating = rating,
        comment = data[ReviewFields.COMMENT] as? String ?: "",
        createdAt = createdAt,
    )
}

/** Média e quantidade calculadas a partir das avaliações recebidas. */
internal fun List<Review>.toRatingSummary(): RatingSummary =
    if (isEmpty()) {
        RatingSummary()
    } else {
        RatingSummary(average = map { it.rating }.average(), count = size)
    }
