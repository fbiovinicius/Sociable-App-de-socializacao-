package com.example.sociable.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.validation.ValidationError
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: ValidationError? = null,
    val emailError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val confirmPasswordError: ValidationError? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val isRegistered: Boolean = false,
    val isGoogleLoading: Boolean = false,
    /** Entrou com o Google numa conta que já existia: vai direto para o app, sem onboarding. */
    val isSignedIn: Boolean = false,
)

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, error = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = null,
                confirmPasswordError = null,
                error = null,
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, error = null) }
    }

    /** Token de ID do Google já obtido pela tela. Conta nova passa pelo onboarding. */
    fun onGoogleToken(idToken: String) {
        val state = _uiState.value
        if (state.isLoading || state.isGoogleLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true, error = null) }
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is AppResult.Success -> _uiState.update {
                    if (result.data.isNewUser) {
                        it.copy(isGoogleLoading = false, isRegistered = true)
                    } else {
                        it.copy(isGoogleLoading = false, isSignedIn = true)
                    }
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isGoogleLoading = false, error = result.error)
                }
            }
        }
    }

    fun onGoogleUnavailable() {
        _uiState.update { it.copy(error = AppError.GOOGLE_SIGN_IN_UNAVAILABLE) }
    }

    fun onRegisterClick() {
        val state = _uiState.value
        if (state.isGoogleLoading) return
        val nameError = Validators.name(state.name)
        val emailError = Validators.email(state.email)
        val passwordError = Validators.password(state.password)
        val confirmError = Validators.confirmPassword(state.password, state.confirmPassword)

        if (nameError != null || emailError != null || passwordError != null || confirmError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signUp(state.name, state.email, state.password)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, isRegistered = true)
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, error = result.error)
                }
            }
        }
    }
}
