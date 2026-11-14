package com.example.sociable.data.repository

import com.example.sociable.core.AppResult
import com.example.sociable.data.model.RatingSummary
import com.example.sociable.data.model.Review
import kotlinx.coroutines.flow.Flow

/**
 * Avaliações/feedback do evento.
 *
 * Implementação Firebase esperada: subcoleção `events/{eventId}/reviews`,
 * com id do documento = "${eventId}_${authorId}" para garantir 1 por usuário.
 * Uma Cloud Function (ou transação) atualiza `ratingAvg`/`ratingCount` no evento.
 */
interface ReviewRepository {

    fun observeReviews(eventId: String): Flow<List<Review>>

    fun observeRating(eventId: String): Flow<RatingSummary>

    suspend fun submitReview(
        eventId: String,
        authorId: String,
        authorName: String,
        rating: Int,
        comment: String,
    ): AppResult<Review>
}
