package com.example.sociable.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sociable.core.AppError
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.core.toAppError
import com.example.sociable.data.model.DateFilter
import com.example.sociable.data.model.EventCategory
import com.example.sociable.data.model.EventFilter
import com.example.sociable.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Item do mapa. Separado de [com.example.sociable.data.model.Event] para a UI
 * do mapa poder evoluir (cluster, marcador, cor) sem mexer no modelo do banco.
 */
data class MapMarker(
    val eventId: String,
    val title: String,
    val category: EventCategory,
    val venueName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val startAt: Instant,
    val endAt: Instant,
    val isHappeningNow: Boolean,
    val isFree: Boolean,
    val price: Double?,
    val goingCount: Int,
    val capacity: Int?,
)

data class MapUiState(
    val markers: List<MapMarker> = emptyList(),
    val selectedEventId: String? = null,
    val isLoading: Boolean = true,
    val error: AppError? = null,
)

/**
 * Lógica do mapa: quais eventos estão acontecendo/acontecerão hoje.
 * A tela placeholder mostra a lista + coordenadas; quando o parceiro integrar
 * o Google Maps, ele só consome `markers` e `onMarkerSelected`.
 */
class MapViewModel(
    private val eventRepository: EventRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            eventRepository.observeEvents(EventFilter(dateFilter = DateFilter.TODAY))
                .catch { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.toAppError())
                    }
                }
                .collect { events ->
                    val now = timeProvider.now()
                    // Segredinhos não aparecem no mapa para não revelar o local.
                    val markers = events.filterNot { it.isSecret }.map { event ->
                        MapMarker(
                            eventId = event.id,
                            title = event.title,
                            category = event.category,
                            venueName = event.venueName,
                            address = event.address,
                            latitude = event.latitude,
                            longitude = event.longitude,
                            startAt = event.startAt,
                            endAt = event.endAt,
                            isHappeningNow = event.isHappeningAt(now),
                            isFree = event.isFree,
                            price = event.price,
                            goingCount = event.goingCount,
                            capacity = event.capacity,
                        )
                    }
                    _uiState.update { it.copy(markers = markers, isLoading = false) }
                }
        }
    }

    fun onMarkerSelected(eventId: String?) {
        _uiState.update { it.copy(selectedEventId = eventId) }
    }
}
