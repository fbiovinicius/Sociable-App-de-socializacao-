package com.example.sociable.feature.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sociable.core.AppError
import com.example.sociable.core.toAppError
import com.example.sociable.data.model.Community
import com.example.sociable.data.model.CommunityDraft
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.CommunityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommunityListUiState(
    val availableCommunities: List<Community> = emptyList(),
    val myCommunities: List<Community> = emptyList(),
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val isCreating: Boolean = false,
)

class CommunityListViewModel(
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityListUiState())
    val uiState: StateFlow<CommunityListUiState> = _uiState.asStateFlow()

    init {
        val userId = authRepository.currentUser.value?.id.orEmpty() ?: ""

        viewModelScope.launch {
            communityRepository.observeCommunities()
                .catch { _ -> emit(emptyList()) }
                .collect { communities ->
                    _uiState.update {
                        it.copy(
                            availableCommunities = communities,
                            isLoading = false,
                        )
                    }
                }
        }

        viewModelScope.launch {
            communityRepository.observeMyCommunities(userId)
                .collect { communities ->
                    _uiState.update {
                        it.copy(myCommunities = communities)
                    }
                }
        }
    }

    fun joinCommunity(communityId: String) {
        val user = authRepository.currentUser.value
        if (user == null) {
            _uiState.update { it.copy(error = com.example.sociable.core.AppError.UNAUTHENTICATED) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = communityRepository.joinCommunity(communityId, user.id)) {
                is com.example.sociable.core.AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
                is com.example.sociable.core.AppResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }

    fun createCommunity(draft: CommunityDraft) {
        val user = authRepository.currentUser.value
        if (user == null) {
            _uiState.update { it.copy(error = com.example.sociable.core.AppError.UNAUTHENTICATED) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, error = null) }
            when (val result = communityRepository.createCommunity(draft, user.id)) {
                is com.example.sociable.core.AppResult.Success -> {
                    _uiState.update { it.copy(isCreating = false) }
                }
                is com.example.sociable.core.AppResult.Failure -> {
                    _uiState.update { it.copy(isCreating = false, error = result.error) }
                }
            }
        }
    }
}