package com.example.sociable.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.toAppError
import com.example.sociable.core.validation.ValidationError
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.model.Interest
import com.example.sociable.data.model.LifeStage
import com.example.sociable.data.model.LookingFor
import com.example.sociable.data.model.LookingForRules
import com.example.sociable.data.model.SocialVibe
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val interests: Set<Interest> = emptySet(),
    val lifeStage: LifeStage? = null,
    val lookingFor: Set<LookingFor> = emptySet(),
    val socialVibe: SocialVibe? = null,
    val nameError: ValidationError? = null,
    val bioError: ValidationError? = null,
    val photoUrlError: ValidationError? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: AppError? = null,
    val isSaved: Boolean = false,
    val isSignedOut: Boolean = false,
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val userId: String? = authRepository.currentUser.value?.id

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        val id = userId
        if (id == null) {
            _uiState.update {
                it.copy(isLoading = false, error = AppError.UNAUTHENTICATED)
            }
        } else {
            viewModelScope.launch {
                userRepository.observeUser(id)
                    .catch { throwable ->
                        _uiState.update {
                            it.copy(isLoading = false, error = throwable.toAppError())
                        }
                    }
                    .collect { user ->
                        if (user != null) {
                            _uiState.update {
                                it.copy(
                                    name = user.name,
                                    email = user.email,
                                    bio = user.bio,
                                    photoUrl = user.photoUrl.orEmpty(),
                                    interests = user.interests,
                                    lifeStage = user.lifeStage,
                                    lookingFor = user.lookingFor,
                                    socialVibe = user.socialVibe,
                                    isLoading = false,
                                )
                            }
                        }
                    }
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, error = null, isSaved = false) }
    }

    fun onBioChange(value: String) {
        _uiState.update { it.copy(bio = value, bioError = null, error = null, isSaved = false) }
    }

    fun onPhotoUrlChange(value: String) {
        _uiState.update { it.copy(photoUrl = value, photoUrlError = null, error = null, isSaved = false) }
    }

    fun onInterestToggle(interest: Interest) {
        _uiState.update { state ->
            val interests = if (interest in state.interests) {
                state.interests - interest
            } else {
                state.interests + interest
            }
            state.copy(interests = interests, isSaved = false)
        }
    }

    fun onLifeStageSelect(value: LifeStage) {
        _uiState.update { state ->
            state.copy(
                lifeStage = if (state.lifeStage == value) null else value,
                isSaved = false,
            )
        }
    }

    fun onLookingForToggle(value: LookingFor) {
        _uiState.update { state ->
            val lookingFor = when {
                value in state.lookingFor -> state.lookingFor - value
                state.lookingFor.size >= LookingForRules.MAX_CHOICES -> state.lookingFor
                else -> state.lookingFor + value
            }
            state.copy(lookingFor = lookingFor, isSaved = false)
        }
    }

    fun onSocialVibeSelect(value: SocialVibe) {
        _uiState.update { state ->
            state.copy(
                socialVibe = if (state.socialVibe == value) null else value,
                isSaved = false,
            )
        }
    }

    fun onSaveClick() {
        val id = userId
        if (id == null) {
            _uiState.update { it.copy(error = AppError.UNAUTHENTICATED) }
            return
        }
        val state = _uiState.value
        val nameError = Validators.name(state.name)
        val bioError = Validators.bio(state.bio)
        val photoUrlError = Validators.imageUrl(state.photoUrl)

        if (nameError != null || bioError != null || photoUrlError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    bioError = bioError,
                    photoUrlError = photoUrlError,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, isSaved = false) }

            val profileResult = userRepository.updateProfile(
                userId = id,
                name = state.name,
                bio = state.bio,
                photoUrl = state.photoUrl.takeIf { it.isNotBlank() },
                interests = state.interests,
            )
            if (profileResult is AppResult.Failure) {
                _uiState.update {
                    it.copy(isSaving = false, error = profileResult.error)
                }
                return@launch
            }

            when (
                val socialResult = userRepository.updateSocialProfile(
                    userId = id,
                    lifeStage = state.lifeStage,
                    lookingFor = state.lookingFor,
                    socialVibe = state.socialVibe,
                )
            ) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isSaving = false, isSaved = true)
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isSaving = false, error = socialResult.error)
                }
            }
        }
    }

    fun onSignOutClick() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update { it.copy(isSignedOut = true) }
        }
    }
}
