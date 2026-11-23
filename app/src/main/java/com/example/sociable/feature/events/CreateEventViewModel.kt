package com.example.sociable.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.ManausTime
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.core.validation.ValidationError
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.model.EventCategory
import com.example.sociable.data.model.EventDraft
import com.example.sociable.data.places.AddressSearch
import com.example.sociable.data.places.AddressSuggestion
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.EventRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.Locale

private const val DEFAULT_DURATION_MINUTES = 120
private const val MIN_DURATION_MINUTES = 5
private const val MIN_ADDRESS_QUERY = 3
private const val ADDRESS_DEBOUNCE_MS = 350L
private const val MAX_ADDRESS_SUGGESTIONS = 5

data class CreateEventUiState(
    val startAt: Instant,
    val endAt: Instant,
    val durationMinutes: Int = DEFAULT_DURATION_MINUTES,
    /** O fim foi escolhido à mão e não deve seguir a duração. */
    val isEndCustom: Boolean = false,
    val title: String = "",
    val description: String = "",
    val category: EventCategory = EventCategory.PARTY,
    val venueName: String = "",
    val address: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val isFree: Boolean = true,
    val isSecret: Boolean = false,
    val price: String = "",
    val capacity: String = "",
    val imageUrl: String = "",
    val titleError: ValidationError? = null,
    val descriptionError: ValidationError? = null,
    val venueError: ValidationError? = null,
    val addressError: ValidationError? = null,
    val latitudeError: ValidationError? = null,
    val longitudeError: ValidationError? = null,
    val priceError: ValidationError? = null,
    val capacityError: ValidationError? = null,
    val imageUrlError: ValidationError? = null,
    val periodError: ValidationError? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    /** Id do evento criado ou salvo; a tela navega quando ele aparece. */
    val createdEventId: String? = null,
    /** Diferente de nulo quando a tela está editando um evento existente. */
    val editingEventId: String? = null,
    val isLoadingEvent: Boolean = false,
    /** Quem já confirmou presença: o limite de pessoas não pode ficar abaixo disso. */
    val goingCount: Int = 0,
    /** Sugestões de endereço enquanto a pessoa digita. */
    val addressSuggestions: List<AddressSuggestion> = emptyList(),
)

class CreateEventViewModel(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
    private val addressSearch: AddressSearch,
    timeProvider: TimeProvider,
) : ViewModel() {

    private var addressSearchJob: Job? = null

    private val initialStart = timeProvider.now()
        .truncatedTo(ChronoUnit.HOURS)
        .plus(1, ChronoUnit.HOURS)

    private val _uiState = MutableStateFlow(
        CreateEventUiState(
            startAt = initialStart,
            endAt = initialStart.plus(DEFAULT_DURATION_MINUTES.toLong(), ChronoUnit.MINUTES),
        )
    )
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    /** Carrega um evento existente no formulário. Só o organizador pode editar. */
    fun loadForEdit(eventId: String) {
        if (_uiState.value.editingEventId == eventId) return
        _uiState.update { it.copy(editingEventId = eventId, isLoadingEvent = true, error = null) }
        viewModelScope.launch {
            when (val result = eventRepository.getEvent(eventId)) {
                is AppResult.Success -> {
                    val event = result.data
                    val savedDuration = Duration.between(event.startAt, event.endAt).toMinutes().toInt()
                    val currentUserId = authRepository.currentUser.value?.id
                    if (event.organizerId != currentUserId) {
                        _uiState.update {
                            it.copy(isLoadingEvent = false, error = AppError.PERMISSION_DENIED)
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                title = event.title,
                                description = event.description,
                                category = event.category,
                                venueName = event.venueName,
                                address = event.address,
                                latitude = String.format(Locale.US, "%.6f", event.latitude),
                                longitude = String.format(Locale.US, "%.6f", event.longitude),
                                startAt = event.startAt,
                                endAt = event.endAt,
                                durationMinutes = savedDuration.coerceIn(
                                    MIN_DURATION_MINUTES,
                                    Validators.MAX_EVENT_DURATION_MINUTES.toInt(),
                                ),
                                // Fim fora do padrão (ou mais longo que o limite) fica como está.
                                isEndCustom = savedDuration !in
                                    MIN_DURATION_MINUTES..Validators.MAX_EVENT_DURATION_MINUTES.toInt(),
                                isFree = event.isFree,
                                isSecret = event.isSecret,
                                price = event.price?.let(::formatPriceInput).orEmpty(),
                                capacity = event.capacity?.toString().orEmpty(),
                                imageUrl = event.imageUrl.orEmpty(),
                                goingCount = event.goingCount,
                                isLoadingEvent = false,
                            )
                        }
                    }
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isLoadingEvent = false, error = result.error)
                }
            }
        }
    }

    private fun formatPriceInput(price: Double): String =
        if (price % 1.0 == 0.0) price.toLong().toString() else price.toString()

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, titleError = null, error = null) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value, descriptionError = null, error = null) }
    }

    fun onCategoryChange(category: EventCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun onVenueChange(value: String) {
        _uiState.update { it.copy(venueName = value, venueError = null, error = null) }
    }

    fun onAddressChange(value: String) {
        _uiState.update {
            it.copy(
                address = value,
                addressError = null,
                error = null,
                addressSuggestions = if (value.trim().length >= MIN_ADDRESS_QUERY) {
                    it.addressSuggestions
                } else {
                    emptyList()
                },
            )
        }
        addressSearchJob?.cancel()
        val query = value.trim()
        if (query.length < MIN_ADDRESS_QUERY) return
        addressSearchJob = viewModelScope.launch {
            // Espera a pessoa parar de digitar antes de consultar (e cobrar) o Places.
            delay(ADDRESS_DEBOUNCE_MS)
            val result = addressSearch.suggest(query)
            _uiState.update {
                it.copy(
                    addressSuggestions = (result as? AppResult.Success)?.data.orEmpty()
                        .take(MAX_ADDRESS_SUGGESTIONS),
                )
            }
        }
    }

    /** A pessoa tocou numa sugestão: preenche o endereço e marca o ponto no mapa. */
    fun onAddressSuggestionSelected(suggestion: AddressSuggestion) {
        addressSearchJob?.cancel()
        _uiState.update {
            it.copy(
                address = suggestion.fullText,
                addressError = null,
                error = null,
                addressSuggestions = emptyList(),
            )
        }
        viewModelScope.launch {
            when (val result = addressSearch.resolve(suggestion)) {
                is AppResult.Success -> {
                    val place = result.data
                    _uiState.update {
                        it.copy(
                            address = place.address,
                            latitude = String.format(Locale.US, "%.6f", place.latitude),
                            longitude = String.format(Locale.US, "%.6f", place.longitude),
                            latitudeError = null,
                            longitudeError = null,
                        )
                    }
                }
                is AppResult.Failure -> _uiState.update { it.copy(error = result.error) }
            }
        }
    }

    /** Fecha a lista de sugestões sem alterar o texto digitado. */
    fun onDismissAddressSuggestions() {
        addressSearchJob?.cancel()
        _uiState.update { it.copy(addressSuggestions = emptyList()) }
    }

    /** Preenche o endereço sugerido pelo mapa sem sobrescrever o que a pessoa já digitou. */
    fun onAddressIfBlank(value: String) {
        _uiState.update {
            if (it.address.isBlank()) it.copy(address = value, addressError = null) else it
        }
    }

    /** Ponto escolhido no mapa. Seis casas decimais dão precisão de cerca de 10 cm. */
    fun onLocationPicked(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(
                latitude = String.format(Locale.US, "%.6f", latitude),
                longitude = String.format(Locale.US, "%.6f", longitude),
                latitudeError = null,
                longitudeError = null,
                error = null,
            )
        }
    }

    /** Nova data mantendo a hora de início escolhida. */
    fun onDateChange(date: LocalDate) {
        _uiState.update { state ->
            val start = ManausTime.toInstant(date, ManausTime.localTime(state.startAt))
            state.withStart(start)
        }
    }

    /** Nova hora de início mantendo a data escolhida. */
    fun onStartTimeChange(time: LocalTime) {
        _uiState.update { state ->
            val start = ManausTime.toInstant(ManausTime.localDate(state.startAt), time)
            state.withStart(start)
        }
    }

    /** Duração em minutos; o fim é recalculado a partir do início. */
    fun onDurationChange(minutes: Int) {
        val safe = minutes.coerceIn(MIN_DURATION_MINUTES, Validators.MAX_EVENT_DURATION_MINUTES.toInt())
        _uiState.update {
            it.copy(
                durationMinutes = safe,
                isEndCustom = false,
                endAt = it.startAt.plus(safe.toLong(), ChronoUnit.MINUTES),
                periodError = null,
            )
        }
    }

    /** Volta a calcular o fim pela duração, descartando o ajuste manual. */
    fun onUseDuration() {
        _uiState.update {
            it.copy(
                isEndCustom = false,
                endAt = it.startAt.plus(it.durationMinutes.toLong(), ChronoUnit.MINUTES),
                periodError = null,
            )
        }
    }

    fun onEndDateChange(date: LocalDate) {
        _uiState.update { state ->
            val end = ManausTime.toInstant(date, ManausTime.localTime(state.endAt))
            state.copy(endAt = end, isEndCustom = true, periodError = null)
        }
    }

    fun onEndTimeChange(time: LocalTime) {
        _uiState.update { state ->
            val end = ManausTime.toInstant(ManausTime.localDate(state.endAt), time)
            state.copy(endAt = end, isEndCustom = true, periodError = null)
        }
    }

    private fun CreateEventUiState.withStart(start: Instant): CreateEventUiState =
        if (isEndCustom) {
            copy(startAt = start, periodError = null)
        } else {
            copy(
                startAt = start,
                endAt = start.plus(durationMinutes.toLong(), ChronoUnit.MINUTES),
                periodError = null,
            )
        }

    fun onFreeChange(isFree: Boolean) {
        _uiState.update {
            it.copy(
                isFree = isFree,
                price = if (isFree) "" else it.price,
                priceError = null,
                error = null,
            )
        }
    }

    fun onSecretChange(isSecret: Boolean) {
        // Um evento já publicado não vira segredinho (nem deixa de ser).
        if (_uiState.value.editingEventId != null) return
        _uiState.update { it.copy(isSecret = isSecret, error = null) }
    }

    fun onPriceChange(value: String) {
        _uiState.update { it.copy(price = value, priceError = null, error = null) }
    }

    fun onCapacityChange(value: String) {
        _uiState.update { it.copy(capacity = value, capacityError = null, error = null) }
    }

    fun onImageUrlChange(value: String) {
        _uiState.update { it.copy(imageUrl = value, imageUrlError = null, error = null) }
    }

    fun onSubmit() {
        val state = _uiState.value
        val titleError = Validators.eventTitle(state.title)
        val descriptionError = Validators.eventDescription(state.description)
        val addressError = Validators.place(state.address)
        val latitudeError = Validators.latitude(state.latitude)
        val longitudeError = Validators.longitude(state.longitude)
        val priceError = when {
            state.isFree -> null
            state.price.isBlank() -> ValidationError.REQUIRED
            else -> Validators.price(state.price)
        }
        val capacityError = Validators.capacity(state.capacity) ?: run {
            val limit = state.capacity.trim().toIntOrNull()
            if (state.editingEventId != null && limit != null && limit < state.goingCount) {
                ValidationError.CAPACITY_BELOW_GOING
            } else {
                null
            }
        }
        val imageUrlError = Validators.imageUrl(state.imageUrl)
        val periodError = Validators.eventDuration(state.startAt, state.endAt)

        val hasErrors = listOf(
            titleError, descriptionError, addressError,
            latitudeError, longitudeError, priceError, capacityError,
            imageUrlError, periodError,
        ).any { it != null }

        if (hasErrors) {
            _uiState.update {
                it.copy(
                    titleError = titleError,
                    descriptionError = descriptionError,
                    addressError = addressError,
                    latitudeError = latitudeError,
                    longitudeError = longitudeError,
                    priceError = priceError,
                    capacityError = capacityError,
                    imageUrlError = imageUrlError,
                    periodError = periodError,
                )
            }
            return
        }

        val user = authRepository.currentUser.value
        if (user == null) {
            _uiState.update { it.copy(error = AppError.UNAUTHENTICATED) }
            return
        }

        val draft = EventDraft(
            title = state.title.trim(),
            description = state.description.trim(),
            category = state.category,
            venueName = state.venueName.trim(),
            address = state.address.trim(),
            latitude = state.latitude.trim().replace(',', '.').toDouble(),
            longitude = state.longitude.trim().replace(',', '.').toDouble(),
            startAt = state.startAt,
            endAt = state.endAt,
            isFree = state.isFree,
            price = if (state.isFree) null else state.price.trim().replace(',', '.').toDoubleOrNull(),
            capacity = state.capacity.trim().takeIf { it.isNotEmpty() }?.toIntOrNull(),
            imageUrl = state.imageUrl.trim().takeIf { it.isNotEmpty() },
            organizerId = user.id,
            organizerName = user.name,
            isSecret = state.isSecret,
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val editingId = state.editingEventId
            val result = if (editingId != null) {
                eventRepository.updateEvent(editingId, draft)
            } else {
                eventRepository.createEvent(draft)
            }
            when (result) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, createdEventId = result.data.id)
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, error = result.error)
                }
            }
        }
    }
}
