package com.example.sociable.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sociable.core.AppError
import com.example.sociable.core.toAppError
import com.example.sociable.data.model.DateFilter
import com.example.sociable.data.model.Event
import com.example.sociable.data.model.EventCategory
import com.example.sociable.data.model.EventFilter
import com.example.sociable.data.repository.EventRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventListUiState(
    val events: List<Event> = emptyList(),
    val filter: EventFilter = EventFilter(),
    val isLoading: Boolean = true,
    val error: AppError? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class EventListViewModel(
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventListUiState())
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()

    private val filter = MutableStateFlow(EventFilter())

    init {
        viewModelScope.launch {
            filter
                .onEach { current ->
                    _uiState.update { it.copy(filter = current, isLoading = true, error = null) }
                }
                .flatMapLatest { current -> eventRepository.observeEvents(current) }
                .catch { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.toAppError()) }
                }
                .collect { events ->
                    _uiState.update { it.copy(events = events, isLoading = false) }
                }
        }
    }

    fun onQueryChange(value: String) {
        filter.update { it.copy(query = value) }
    }

    fun onCategoryToggle(category: EventCategory) {
        filter.update { current ->
            val categories = if (category in current.categories) {
                current.categories - category
            } else {
                current.categories + category
            }
            current.copy(categories = categories)
        }
    }

    fun onDateFilterChange(dateFilter: DateFilter) {
        filter.update { it.copy(dateFilter = dateFilter) }
    }

    fun onOnlyFreeChange(onlyFree: Boolean) {
        filter.update { it.copy(onlyFree = onlyFree) }
    }

    /** Usado pela aba "Segredinhos" para mostrar só eventos com local oculto. */
    fun onOnlySecretChange(onlySecret: Boolean) {
        filter.update { it.copy(onlySecret = onlySecret) }
    }

    fun clearFilters() {
        filter.value = EventFilter()
    }
}
