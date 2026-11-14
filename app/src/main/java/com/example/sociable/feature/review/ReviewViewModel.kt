package com.example.sociable.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.toAppError
import com.example.sociable.core.validation.ValidationError
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.model.RatingSummary
import com.example.sociable.data.model.Review
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewUiState(
    val reviews: List<Review> = emptyList(),
    val rating: RatingSummary = RatingSummary(),
    val selectedRating: Int = 0,
    val comment: String = "",
    val ratingError: ValidationError? = null,
    val commentError: ValidationError? = null,
    val hasReviewed: Boolean = false,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: AppError? = null,
    val isSubmitted: Boolean = false,
)

class ReviewViewModel(
    private val eventId: String,
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val userId: String? = authRepository.currentUser.value?.id

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                reviewRepository.observeReviews(eventId),
                reviewRepository.observeRating(eventId),
            ) { reviews, rating ->
                reviews to rating
            }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.toAppError())
                    }
                }
                .collect { (reviews, rating) ->
                    _uiState.update {
                        it.copy(
                            reviews = reviews,
                            rating = rating,
                            hasReviewed = reviews.any { review -> review.authorId == userId },
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun onRatingChange(value: Int) {
        _uiState.update { it.copy(selectedRating = value, ratingError = null, error = null) }
    }

    fun onCommentChange(value: String) {
        _uiState.update { it.copy(comment = value, commentError = null, error = null) }
    }

    fun onSubmitClick() {
        val user = authRepository.currentUser.value
        if (user == null) {
            _uiState.update { it.copy(error = AppError.UNAUTHENTICATED) }
            return
        }
        val state = _uiState.value
        val ratingError = Validators.rating(state.selectedRating)
        val commentError = Validators.reviewComment(state.comment)
        if (ratingError != null || commentError != null) {
            _uiState.update {
                it.copy(ratingError = ratingError, commentError = commentError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (
                val result = reviewRepository.submitReview(
                    eventId = eventId,
                    authorId = user.id,
                    authorName = user.name,
                    rating = state.selectedRating,
                    comment = state.comment,
                )
            ) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isSubmitting = false, isSubmitted = true, comment = "")
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.error)
                }
            }
        }
    }
}
