package com.example.sociable.data.model

/** Média e quantidade de avaliações de um evento. */
data class RatingSummary(
    val average: Double = 0.0,
    val count: Int = 0,
) {
    val hasReviews: Boolean get() = count > 0
}
