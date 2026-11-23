package com.example.sociable.data.fake

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.model.User
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.SocialSignInResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Implementação em memória de [AuthRepository].
 * Simula latência para os estados de loading aparecerem na UI.
 */
class FakeAuthRepository(
    private val db: FakeDatabase,
    private val timeProvider: TimeProvider,
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override suspend fun signIn(email: String, password: String): AppResult<User> {
        delay(350)
        val normalized = email.trim().lowercase()
        val user = db.users.value.firstOrNull { it.email.lowercase() == normalized }
            ?: return AppResult.Failure(AppError.INVALID_CREDENTIALS)
        val expectedPassword = db.credentials[normalized]
            ?: return AppResult.Failure(AppError.INVALID_CREDENTIALS)
        if (expectedPassword != password) {
            return AppResult.Failure(AppError.INVALID_CREDENTIALS)
        }
        _currentUser.value = user
        return AppResult.Success(user)
    }

    override suspend fun signUp(name: String, email: String, password: String): AppResult<User> {
        delay(450)
        val normalized = email.trim().lowercase()
        if (db.users.value.any { it.email.lowercase() == normalized }) {
            return AppResult.Failure(AppError.EMAIL_ALREADY_IN_USE)
        }
        if (password.length < Validators.PASSWORD_MIN) {
            return AppResult.Failure(AppError.WEAK_PASSWORD)
        }
        val user = User(
            id = "user-${db.users.value.size + 1}-${timeProvider.now().toEpochMilli()}",
            name = name.trim(),
            email = normalized,
            bio = "",
            photoUrl = null,
            interests = emptySet(),
            createdAt = timeProvider.now(),
        )
        db.users.update { it + user }
        db.credentials[normalized] = password
        _currentUser.value = user
        return AppResult.Success(user)
    }

    /** No fake, o "token" do Google é só um apelido: a conta é `<token>@google.test`. */
    override suspend fun signInWithGoogle(idToken: String): AppResult<SocialSignInResult> {
        delay(350)
        if (idToken.isBlank()) return AppResult.Failure(AppError.INVALID_CREDENTIALS)
        val email = "${idToken.trim().lowercase()}@google.test"
        val existing = db.users.value.firstOrNull { it.email == email }
        if (existing != null) {
            _currentUser.value = existing
            return AppResult.Success(SocialSignInResult(existing, isNewUser = false))
        }
        val user = User(
            id = "user-${db.users.value.size + 1}-${timeProvider.now().toEpochMilli()}",
            name = idToken.trim(),
            email = email,
            createdAt = timeProvider.now(),
        )
        db.users.update { it + user }
        _currentUser.value = user
        return AppResult.Success(SocialSignInResult(user, isNewUser = true))
    }

    override suspend fun signOut() {
        delay(100)
        _currentUser.value = null
    }

    override suspend fun sendPasswordReset(email: String): AppResult<Unit> {
        delay(300)
        val normalized = email.trim().lowercase()
        return if (db.users.value.any { it.email.lowercase() == normalized }) {
            AppResult.Success(Unit)
        } else {
            AppResult.Failure(AppError.USER_NOT_FOUND)
        }
    }
}
