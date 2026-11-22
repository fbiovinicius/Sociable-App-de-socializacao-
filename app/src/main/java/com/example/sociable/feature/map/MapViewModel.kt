@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.sociable.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.core.toAppError
import com.example.sociable.data.model.Attendee
import com.example.sociable.data.model.DateFilter
import com.example.sociable.data.model.EventCategory
import com.example.sociable.data.model.EventFilter
import com.example.sociable.data.model.ReportReason
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.EventRepository
import com.example.sociable.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    /** Quem confirmou presença no evento selecionado (prévia com fotos). */
    val attendees: List<Attendee> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingAttendees: Boolean = false,
    val isReporting: Boolean = false,
    val reportSent: Boolean = false,
    val error: AppError? = null,
    val attendeesError: AppError? = null,
    val reportError: AppError? = null,
)

/**
 * Lógica do mapa: quais eventos estão acontecendo/acontecerão hoje.
 * A tela do mapa (Google Maps Compose) só consome `markers` e `onMarkerSelected`.
 */
class MapViewModel(
    private val eventRepository: EventRepository,
    private val reportRepository: ReportRepository,
    private val authRepository: AuthRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    /** Evento marcado no mapa; dirige a carga da prévia de participantes. */
    private val selectedEventId = MutableStateFlow<String?>(null)

    /** Permite repetir a carga quando a pessoa toca novamente no mesmo pin. */
    private val attendeeReloadVersion = MutableStateFlow(0L)

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
                    val currentId = _uiState.value.selectedEventId
                        ?.takeIf { id -> markers.any { it.eventId == id } }
                        ?: markers.firstOrNull()?.eventId
                    selectEvent(currentId)
                    _uiState.update {
                        it.copy(
                            markers = markers,
                            isLoading = false,
                        )
                    }
                }
        }

        // Prévia de participantes: só recarrega quando o evento marcado muda.
        viewModelScope.launch {
            selectedEventId
                .combine(attendeeReloadVersion) { eventId, _ -> eventId }
                .flatMapLatest { eventId ->
                    if (eventId == null) {
                        flowOf(AttendeesLoad(eventId = null))
                    } else {
                        eventRepository.observeAttendees(eventId)
                            .map { attendees -> AttendeesLoad(eventId, attendees) }
                            .onEmpty { emit(AttendeesLoad(eventId = eventId)) }
                            .catch { throwable ->
                                emit(
                                    AttendeesLoad(
                                        eventId = eventId,
                                        error = throwable.toAppError(),
                                    ),
                                )
                            }
                    }
                }
                .collect { load ->
                    _uiState.update { state ->
                        // Uma emissão antiga pode chegar durante a troca do pin.
                        // O id garante que ela não substitua a prévia atual.
                        if (state.selectedEventId != load.eventId) {
                            state
                        } else {
                            state.copy(
                                attendees = load.attendees,
                                isLoadingAttendees = false,
                                attendeesError = load.error,
                            )
                        }
                    }
                }
        }
    }

    fun onMarkerSelected(eventId: String?) {
        if (!selectEvent(eventId)) {
            // StateFlow não reemite o mesmo valor. O contador reinicia a
            // observação para permitir retry ao tocar novamente no pin.
            _uiState.update {
                it.copy(
                    isLoadingAttendees = eventId != null,
                    attendeesError = null,
                )
            }
            attendeeReloadVersion.update { it + 1 }
        }
    }

    /**
     * Mantém o evento selecionado e a carga da prévia em sincronia. Ao trocar
     * de evento, a lista anterior é removida imediatamente para não atribuir
     * participantes ao evento errado.
     *
     * @return `true` quando o evento selecionado mudou.
     */
    private fun selectEvent(eventId: String?): Boolean {
        val changed = _uiState.value.selectedEventId != eventId
        if (changed) {
            _uiState.update {
                it.copy(
                    selectedEventId = eventId,
                    attendees = emptyList(),
                    isLoadingAttendees = eventId != null,
                    attendeesError = null,
                )
            }
        }
        selectedEventId.value = eventId
        return changed
    }

    private data class AttendeesLoad(
        val eventId: String?,
        val attendees: List<Attendee> = emptyList(),
        val error: AppError? = null,
    )

    /** Denuncia o evento selecionado. O texto do aviso fica na camada de UI. */
    fun reportEvent(reason: ReportReason, comment: String) {
        val currentState = _uiState.value
        if (currentState.isReporting) return
        val eventId = currentState.selectedEventId ?: return
        val user = authRepository.currentUser.value
        if (user == null) {
            _uiState.update { it.copy(reportError = AppError.UNAUTHENTICATED) }
            return
        }

        // Bloqueia o envio antes de iniciar a corrotina para impedir toques
        // duplicados enquanto a primeira denúncia ainda está em andamento.
        _uiState.update {
            it.copy(isReporting = true, reportError = null, reportSent = false)
        }
        viewModelScope.launch {
            when (val result = reportRepository.reportEvent(eventId, user.id, reason, comment)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isReporting = false, reportSent = true)
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isReporting = false, reportError = result.error)
                }
            }
        }
    }

    fun clearReportState() {
        _uiState.update { it.copy(reportSent = false, reportError = null) }
    }
}
