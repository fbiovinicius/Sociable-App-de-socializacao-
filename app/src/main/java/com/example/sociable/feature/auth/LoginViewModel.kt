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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val isLoggedIn: Boolean = false,
    val resetEmailSent: Boolean = false,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, error = null) }
    }

    fun onLoginClick() {
        val state = _uiState.value
        val emailError = Validators.email(state.email)
        val passwordError = Validators.password(state.password)
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signIn(state.email, state.password)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, isLoggedIn = true)
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, error = result.error)
                }
            }
        }
    }

    fun onForgotPasswordClick() {
        val emailError = Validators.email(_uiState.value.email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError, resetEmailSent = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, resetEmailSent = false) }
            when (val result = authRepository.sendPasswordReset(_uiState.value.email)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, resetEmailSent = true)
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, error = result.error)
                }
            }
        }
    }
}
