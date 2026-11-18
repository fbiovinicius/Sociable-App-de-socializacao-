package com.example.sociable.data.firebase

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.model.RatingSummary
import com.example.sociable.data.model.Review
import com.example.sociable.data.repository.ReviewRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Avaliações em `events/{eventId}/reviews/{authorId}`.
 *
 * O id do documento é o uid do autor: as regras só permitem criar (nunca
 * alterar), então cada pessoa avalia um evento uma única vez. A média é
 * calculada aqui no cliente a partir das avaliações; uma Cloud Function para
 * desnormalizar a média no evento fica para quando o projeto migrar para o
 * plano Blaze.
 */
class FirestoreReviewRepository(
    private val firestore: FirebaseFirestore,
    private val timeProvider: TimeProvider,
) : ReviewRepository {

    override fun observeReviews(eventId: String): Flow<List<Review>> = callbackFlow {
        val query = reviews(eventId)
            .orderBy(ReviewFields.CREATED_AT, Query.Direction.DESCENDING)
            .limit(REVIEW_LIMIT)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(
                snapshot?.documents.orEmpty().mapNotNull { document ->
                    document.toDomainReview(eventId)
                }
            )
        }

        awaitClose { registration.remove() }
    }

    override fun observeRating(eventId: String): Flow<RatingSummary> =
        observeReviews(eventId).map { list -> list.toRatingSummary() }

    override suspend fun submitReview(
        eventId: String,
        authorId: String,
        authorName: String,
        rating: Int,
        comment: String,
    ): AppResult<Review> {
        if (Validators.rating(rating) != null || Validators.reviewComment(comment) != null) {
            return AppResult.Failure(AppError.INVALID_INPUT)
        }

        return try {
            val reference = reviews(eventId).document(authorId)
            if (reference.get().await().exists()) {
                return AppResult.Failure(AppError.ALREADY_REVIEWED)
            }
            val trimmedComment = comment.trim()
            reference.set(
                mapOf(
                    ReviewFields.AUTHOR_ID to authorId,
                    ReviewFields.AUTHOR_NAME to authorName.take(AUTHOR_NAME_MAX),
                    ReviewFields.RATING to rating,
                    ReviewFields.COMMENT to trimmedComment,
                    ReviewFields.CREATED_AT to FieldValue.serverTimestamp(),
                )
            ).await()
            AppResult.Success(
                Review(
                    id = reviewId(eventId, authorId),
                    eventId = eventId,
                    authorId = authorId,
                    authorName = authorName,
                    rating = rating,
                    comment = trimmedComment,
                    createdAt = timeProvider.now(),
                )
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Failure(exception.toFirebaseAppError(), exception)
        }
    }

    private fun reviews(eventId: String) =
        firestore.collection(COLLECTION_EVENTS).document(eventId).collection(SUBCOLLECTION_REVIEWS)

    private fun DocumentSnapshot.toDomainReview(eventId: String): Review? =
        getData(DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?.let { data -> mapToReview(eventId = eventId, documentId = id, data = data) }

    private companion object {
        const val COLLECTION_EVENTS = "events"
        const val SUBCOLLECTION_REVIEWS = "reviews"
        const val REVIEW_LIMIT = 100L
        const val AUTHOR_NAME_MAX = 100
    }
}
