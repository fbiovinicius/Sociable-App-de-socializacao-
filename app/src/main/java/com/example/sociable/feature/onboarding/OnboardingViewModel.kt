package com.example.sociable.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.data.model.LifeStage
import com.example.sociable.data.model.LookingFor
import com.example.sociable.data.model.LookingForRules
import com.example.sociable.data.model.SocialVibe
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado do wizard mostrado logo após o cadastro.
 *
 * As respostas ficam só na memória até o último passo; ao concluir, tudo é
 * gravado de uma vez em `users/{uid}` via [UserRepository.updateSocialProfile].
 */
data class OnboardingUiState(
    val step: Int = 0,
    val lifeStage: LifeStage? = null,
    val lookingFor: Set<LookingFor> = emptySet(),
    val socialVibe: SocialVibe? = null,
    val isSaving: Boolean = false,
    val error: AppError? = null,
    val isDone: Boolean = false,
)

class OnboardingViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onLifeStageSelect(value: LifeStage) {
        _uiState.update { state ->
            state.copy(
                lifeStage = if (state.lifeStage == value) null else value,
                error = null,
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
            state.copy(lookingFor = lookingFor, error = null)
        }
    }

    fun onSocialVibeSelect(value: SocialVibe) {
        _uiState.update { state ->
            state.copy(
                socialVibe = if (state.socialVibe == value) null else value,
                error = null,
            )
        }
    }

    fun onNext() {
        val state = _uiState.value
        if (state.isSaving || state.isDone) return
        if (state.step < LAST_STEP) {
            _uiState.update { it.copy(step = it.step + 1, error = null) }
        } else {
            save()
        }
    }

    fun onBack() {
        val state = _uiState.value
        if (state.isSaving || state.step == 0) return
        _uiState.update { it.copy(step = it.step - 1, error = null) }
    }

    /**
     * Pula a etapa atual descartando a escolha dela e avança. No último passo,
     * conclui o onboarding mantendo as respostas dos passos anteriores.
     */
    fun onSkipStep() {
        val state = _uiState.value
        if (state.isSaving || state.isDone) return
        when (state.step) {
            0 -> _uiState.update {
                it.copy(lifeStage = null, step = 1, error = null)
            }

            1 -> _uiState.update {
                it.copy(lookingFor = emptySet(), step = 2, error = null)
            }

            else -> {
                _uiState.update { it.copy(socialVibe = null, error = null) }
                save()
            }
        }
    }

    private fun save() {
        val userId = authRepository.currentUser.value?.id
        if (userId == null) {
            _uiState.update { it.copy(error = AppError.UNAUTHENTICATED) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val state = _uiState.value
            when (
                val result = userRepository.updateSocialProfile(
                    userId = userId,
                    lifeStage = state.lifeStage,
                    lookingFor = state.lookingFor,
                    socialVibe = state.socialVibe,
                )
            ) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isSaving = false, isDone = true)
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isSaving = false, error = result.error)
                }
            }
        }
    }

    companion object {
        const val STEP_COUNT = 3
        private const val LAST_STEP = STEP_COUNT - 1
    }
}
