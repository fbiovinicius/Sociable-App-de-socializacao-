package com.example.sociable.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.toAppError
import com.example.sociable.core.validation.ValidationError
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.model.Message
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.ChatRepository
import com.example.sociable.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val eventTitle: String = "",
    val goingCount: Int = 0,
    val input: String = "",
    val inputError: ValidationError? = null,
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: AppError? = null,
    val currentUserId: String = "",
)

class ChatViewModel(
    private val eventId: String,
    private val chatRepository: ChatRepository,
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatUiState(currentUserId = authRepository.currentUser.value?.id.orEmpty())
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                chatRepository.observeMessages(eventId),
                eventRepository.observeEvent(eventId),
            ) { messages, event ->
                messages to event
            }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.toAppError())
                    }
                }
                .collect { (messages, event) ->
                    _uiState.update {
                        it.copy(
                            messages = messages,
                            eventTitle = event?.title.orEmpty(),
                            goingCount = event?.goingCount ?: 0,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun onInputChange(value: String) {
        _uiState.update { it.copy(input = value, inputError = null, error = null) }
    }

    fun onSendClick() {
        val state = _uiState.value
        val inputError = Validators.message(state.input)
        if (inputError != null) {
            _uiState.update { it.copy(inputError = inputError) }
            return
        }

        val user = authRepository.currentUser.value
        if (user == null) {
            _uiState.update { it.copy(error = AppError.UNAUTHENTICATED) }
            return
        }

        val text = state.input.trim()
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            when (
                val result = chatRepository.sendMessage(
                    eventId = eventId,
                    authorId = user.id,
                    authorName = user.name,
                    text = text,
                )
            ) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isSending = false, input = "", inputError = null)
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isSending = false, error = result.error)
                }
            }
        }
    }
}
