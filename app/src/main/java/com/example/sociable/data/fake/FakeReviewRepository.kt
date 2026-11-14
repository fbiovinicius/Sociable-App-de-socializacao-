package com.example.sociable.data.fake

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.model.RatingSummary
import com.example.sociable.data.model.Review
import com.example.sociable.data.repository.ReviewRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeReviewRepository(
    private val db: FakeDatabase,
    private val timeProvider: TimeProvider,
) : ReviewRepository {

    override fun observeReviews(eventId: String): Flow<List<Review>> =
        db.reviews.map { all ->
            all[eventId].orEmpty().sortedByDescending { it.createdAt }
        }

    override fun observeRating(eventId: String): Flow<RatingSummary> =
        observeReviews(eventId).map { reviews ->
            if (reviews.isEmpty()) {
                RatingSummary()
            } else {
                RatingSummary(
                    average = reviews.map { it.rating }.average(),
                    count = reviews.size,
                )
            }
        }

    override suspend fun submitReview(
        eventId: String,
        authorId: String,
        authorName: String,
        rating: Int,
        comment: String,
    ): AppResult<Review> {
        delay(300)
        if (Validators.rating(rating) != null) {
            return AppResult.Failure(AppError.INVALID_INPUT)
        }
        if (Validators.reviewComment(comment) != null) {
            return AppResult.Failure(AppError.INVALID_INPUT)
        }
        val existing = db.reviews.value[eventId].orEmpty()
        if (existing.any { it.authorId == authorId }) {
            return AppResult.Failure(AppError.ALREADY_REVIEWED)
        }
        val review = Review(
            id = "${eventId}_$authorId",
            eventId = eventId,
            authorId = authorId,
            authorName = authorName,
            rating = rating,
            comment = comment.trim(),
            createdAt = timeProvider.now(),
        )
        db.reviews.update { all ->
            all + (eventId to (all[eventId].orEmpty() + review))
        }
        return AppResult.Success(review)
    }
}
