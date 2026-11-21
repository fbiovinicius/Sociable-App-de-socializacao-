package com.example.sociable.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.notifications.EventReminder
import com.example.sociable.core.notifications.ReminderScheduler
import com.example.sociable.core.toAppError
import com.example.sociable.data.model.Event
import com.example.sociable.data.model.EventStatus
import com.example.sociable.data.model.RatingSummary
import com.example.sociable.data.model.RsvpInfo
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.EventRepository
import com.example.sociable.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val event: Event? = null,
    val rsvp: RsvpInfo = RsvpInfo(),
    val rating: RatingSummary = RatingSummary(),
    val isLoading: Boolean = true,
    val isTogglingRsvp: Boolean = false,
    val isCancelling: Boolean = false,
    val error: AppError? = null,
) {
    val isCancelled: Boolean get() = event?.status == EventStatus.CANCELLED
}

class EventDetailViewModel(
    private val eventId: String,
    private val eventRepository: EventRepository,
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val currentUserId: String? = authRepository.currentUser.value?.id

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    init {
        val userId = authRepository.currentUser.value?.id.orEmpty()
        viewModelScope.launch {
            combine(
                eventRepository.observeEvent(eventId),
                eventRepository.observeRsvp(eventId, userId),
                // A média é um extra: se a leitura das avaliações falhar, o evento
                // continua abrindo, só sem a nota.
                reviewRepository.observeRating(eventId)
                    .catch { emit(RatingSummary()) },
            ) { event, rsvp, rating ->
                Triple(event, rsvp, rating)
            }
                .catch { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.toAppError()) }
                }
                .collect { (event, rsvp, rating) ->
                    // Evento cancelado: o lembrete local não faz mais sentido.
                    if (event?.status == EventStatus.CANCELLED) {
                        reminderScheduler.cancel(eventId)
                    }
                    _uiState.update {
                        it.copy(
                            event = event,
                            rsvp = rsvp,
                            rating = rating,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun onToggleRsvp() {
        val user = authRepository.currentUser.value
        if (user == null) {
            _uiState.update { it.copy(error = AppError.UNAUTHENTICATED) }
            return
        }
        val going = !_uiState.value.rsvp.isGoing
        viewModelScope.launch {
            _uiState.update { it.copy(isTogglingRsvp = true, error = null) }
            when (val result = eventRepository.setRsvp(eventId, user.id, going)) {
                is AppResult.Success -> {
                    updateReminder(going)
                    _uiState.update { it.copy(isTogglingRsvp = false) }
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(isTogglingRsvp = false, error = result.error)
                }
            }
        }
    }

    /**
     * Confirma presença → lembrete local 1 h antes (bloco F1).
     * Cancela presença → remove o lembrete.
     */
    private fun updateReminder(going: Boolean) {
        val event = _uiState.value.event
        when {
            going && event != null -> reminderScheduler.schedule(
                EventReminder(
                    eventId = event.id,
                    title = event.title,
                    startAtMillis = event.startAt.toEpochMilli(),
                ),
            )
            !going -> reminderScheduler.cancel(eventId)
        }
    }

    /** Só o organizador cancela; as regras do Firestore também garantem isso. */
    fun onCancelEvent() {
        val event = _uiState.value.event ?: return
        if (event.organizerId != currentUserId) {
            _uiState.update { it.copy(error = AppError.PERMISSION_DENIED) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, error = null) }
            when (val result = eventRepository.cancelEvent(eventId)) {
                is AppResult.Success -> {
                    reminderScheduler.cancel(eventId)
                    _uiState.update { it.copy(isCancelling = false) }
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(isCancelling = false, error = result.error)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
