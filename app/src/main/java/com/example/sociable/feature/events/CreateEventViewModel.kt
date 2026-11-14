package com.example.sociable.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.core.validation.ValidationError
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.model.EventCategory
import com.example.sociable.data.model.EventDraft
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

data class CreateEventUiState(
    val startAt: Instant,
    val endAt: Instant,
    val title: String = "",
    val description: String = "",
    val category: EventCategory = EventCategory.PARTY,
    val venueName: String = "",
    val address: String = "",
    val latitude: String = "-3.1190",
    val longitude: String = "-60.0217",
    val isFree: Boolean = true,
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
    val createdEventId: String? = null,
)

class CreateEventViewModel(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
    timeProvider: TimeProvider,
) : ViewModel() {

    private val initialStart = timeProvider.now().plus(1, ChronoUnit.HOURS)

    private val _uiState = MutableStateFlow(
        CreateEventUiState(
            startAt = initialStart,
            endAt = initialStart.plus(2, ChronoUnit.HOURS),
        )
    )
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

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
        _uiState.update { it.copy(address = value, addressError = null, error = null) }
    }

    fun onLatitudeChange(value: String) {
        _uiState.update { it.copy(latitude = value, latitudeError = null, error = null) }
    }

    fun onLongitudeChange(value: String) {
        _uiState.update { it.copy(longitude = value, longitudeError = null, error = null) }
    }

    fun onStartShift(hours: Long) {
        _uiState.update {
            it.copy(startAt = it.startAt.plus(hours, ChronoUnit.HOURS), periodError = null)
        }
    }

    fun onEndShift(hours: Long) {
        _uiState.update {
            it.copy(endAt = it.endAt.plus(hours, ChronoUnit.HOURS), periodError = null)
        }
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
        val venueError = Validators.place(state.venueName)
        val addressError = Validators.place(state.address)
        val latitudeError = Validators.latitude(state.latitude)
        val longitudeError = Validators.longitude(state.longitude)
        val priceError = when {
            state.isFree -> null
            state.price.isBlank() -> ValidationError.REQUIRED
            else -> Validators.price(state.price)
        }
        val capacityError = Validators.capacity(state.capacity)
        val imageUrlError = Validators.imageUrl(state.imageUrl)
        val periodError = Validators.eventPeriod(state.startAt, state.endAt)

        val hasErrors = listOf(
            titleError, descriptionError, venueError, addressError,
            latitudeError, longitudeError, priceError, capacityError,
            imageUrlError, periodError,
        ).any { it != null }

        if (hasErrors) {
            _uiState.update {
                it.copy(
                    titleError = titleError,
                    descriptionError = descriptionError,
                    venueError = venueError,
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
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = eventRepository.createEvent(draft)) {
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
