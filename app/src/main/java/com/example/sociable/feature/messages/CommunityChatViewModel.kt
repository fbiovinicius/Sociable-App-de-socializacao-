package com.example.sociable.feature.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.toAppError
import com.example.sociable.core.validation.ValidationError
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.model.Message
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.CommunityRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommunityChatUiState(
    val name: String = "",
    val memberCount: Int = 0,
    val messages: List<Message> = emptyList(),
    val input: String = "",
    val inputError: ValidationError? = null,
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: AppError? = null,
    val currentUserId: String = "",
    val isMember: Boolean = false,
    val isOwner: Boolean = false,
    /** Vira verdadeiro depois de sair; a tela volta para a lista. */
    val hasLeft: Boolean = false,
)

class CommunityChatViewModel(
    private val communityId: String,
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CommunityChatUiState(currentUserId = authRepository.currentUser.value?.id.orEmpty())
    )
    val uiState: StateFlow<CommunityChatUiState> = _uiState.asStateFlow()

    init {
        val userId = _uiState.value.currentUserId
        val community = communityRepository.observeCommunity(communityId)
        val member = communityRepository.observeMemberships(userId)
            .map { communityId in it }
            .distinctUntilChanged()

        @OptIn(ExperimentalCoroutinesApi::class)
        val messages = member.flatMapLatest { isMember ->
            if (isMember) communityRepository.observeMessages(communityId) else flowOf(emptyList())
        }

        viewModelScope.launch {
            combine(community, member, messages) { current, isMember, list ->
                Triple(current, isMember, list)
            }
                .catch { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.toAppError()) }
                }
                .collect { (current, isMember, list) ->
                    _uiState.update {
                        it.copy(
                            name = current?.name.orEmpty(),
                            memberCount = current?.memberCount ?: 0,
                            isOwner = current?.ownerId == userId,
                            isMember = isMember,
                            messages = list,
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
        if (!state.isMember) {
            _uiState.update { it.copy(error = AppError.CHAT_NOT_AVAILABLE) }
            return
        }
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
            when (val result = communityRepository.sendMessage(communityId, user.id, user.name, text)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isSending = false, input = "", inputError = null)
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isSending = false, error = result.error)
                }
            }
        }
    }

    fun onJoinClick() {
        val user = authRepository.currentUser.value
        if (user == null) {
            _uiState.update { it.copy(error = AppError.UNAUTHENTICATED) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            val result = communityRepository.join(communityId, user.id, user.name)
            if (result is AppResult.Failure) _uiState.update { it.copy(error = result.error) }
        }
    }

    fun onLeaveClick() {
        val userId = _uiState.value.currentUserId
        viewModelScope.launch {
            when (val result = communityRepository.leave(communityId, userId)) {
                is AppResult.Success -> _uiState.update { it.copy(hasLeft = true) }
                is AppResult.Failure -> _uiState.update { it.copy(error = result.error) }
            }
        }
    }
}
