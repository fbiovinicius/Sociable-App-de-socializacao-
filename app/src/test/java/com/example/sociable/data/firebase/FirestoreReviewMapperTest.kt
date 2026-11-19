package com.example.sociable.data.firebase

import com.example.sociable.data.model.RatingSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.util.Date

class FirestoreReviewMapperTest {

    private val createdAt = Instant.parse("2026-03-11T10:00:00Z")

    private fun data(rating: Any? = 4L): Map<String, Any?> = mapOf(
        ReviewFields.AUTHOR_ID to "user-1",
        ReviewFields.AUTHOR_NAME to "Ana",
        ReviewFields.RATING to rating,
        ReviewFields.COMMENT to "Muito bom",
        ReviewFields.CREATED_AT to Date.from(createdAt),
    )

    @Test
    fun `mapeia documento completo e usa id composto`() {
        val review = mapToReview("event-1", "user-1", data())!!

        assertEquals("event-1_user-1", review.id)
        assertEquals("user-1", review.authorId)
        assertEquals(4, review.rating)
        assertEquals("Muito bom", review.comment)
        assertEquals(createdAt, review.createdAt)
    }

    @Test
    fun `usa o id do documento quando authorId falta`() {
        val review = mapToReview("event-1", "user-9", data() - ReviewFields.AUTHOR_ID)!!

        assertEquals("user-9", review.authorId)
    }

    @Test
    fun `descarta nota invalida ou data ausente`() {
        assertNull(mapToReview("e", "u", data(rating = 0L)))
        assertNull(mapToReview("e", "u", data(rating = 6L)))
        assertNull(mapToReview("e", "u", data(rating = "5")))
        assertNull(mapToReview("e", "u", data() - ReviewFields.CREATED_AT))
    }

    @Test
    fun `calcula media e quantidade`() {
        val reviews = listOf(5L, 4L, 3L).mapIndexed { index, rating ->
            mapToReview("e", "u$index", data(rating) + (ReviewFields.AUTHOR_ID to "u$index"))!!
        }

        val summary = reviews.toRatingSummary()

        assertEquals(4.0, summary.average, 0.0001)
        assertEquals(3, summary.count)
    }

    @Test
    fun `lista vazia gera resumo vazio`() {
        assertEquals(RatingSummary(), emptyList<com.example.sociable.data.model.Review>().toRatingSummary())
    }
}
