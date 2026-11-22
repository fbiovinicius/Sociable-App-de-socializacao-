package com.example.sociable.feature.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sociable.data.model.Event
import com.example.sociable.data.model.EventFilter
import com.example.sociable.data.model.EventStatus
import com.example.sociable.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

/** Evento em que o usuário está confirmado e possui chat. */
data class ConversationEvent(
    val eventId: String,
    val title: String,
    val categoryLabel: String,
    val venueName: String,
    val address: String,
    val startAt: Instant,
    val goingCount: Int,
)

data class MessagesUiState(
    val conversations: List<ConversationEvent> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class MessagesViewModel(
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val now = Instant.now()
            eventRepository.observeEvents(
                EventFilter(dateFilter = com.example.sociable.data.model.DateFilter.NEXT_7_DAYS)
            )
                .catch { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.message) }
                }
                .collect { events ->
                    val upcoming = events.filter { e ->
                        e.status == EventStatus.ACTIVE && !e.isFinishedAt(now) && !e.isSecret
                    }
                    val conversations = upcoming.map { event ->
                        ConversationEvent(
                            eventId = event.id,
                            title = event.title,
                            categoryLabel = event.category.label,
                            venueName = event.venueName,
                            address = event.address,
                            startAt = event.startAt,
                            goingCount = event.goingCount,
                        )
                    }
                    _uiState.update {
                        it.copy(
                            conversations = conversations,
                            isLoading = false,
                        )
                    }
                }
        }
    }
}