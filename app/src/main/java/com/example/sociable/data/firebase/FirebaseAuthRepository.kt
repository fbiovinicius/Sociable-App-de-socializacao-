package com.example.sociable.data.firebase

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.data.model.User
import com.example.sociable.data.repository.AuthRepository
import com.example.sociable.data.repository.SocialSignInResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/** Implementação real de autenticação usando Firebase Authentication + perfil no Firestore. */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val timeProvider: TimeProvider,
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private var profileListener: com.google.firebase.firestore.ListenerRegistration? = null

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        profileListener?.remove()
        profileListener = null
        if (firebaseUser == null) {
            _currentUser.value = null
        } else {
            observeProfile(firebaseUser)
        }
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    private fun observeProfile(firebaseUser: FirebaseUser) {
        profileListener = firestore.collection(USERS).document(firebaseUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _currentUser.value = null
                    return@addSnapshotListener
                }
                _currentUser.value = snapshot?.toDomainUser()
            }
    }

    override suspend fun signIn(email: String, password: String): AppResult<User> {
        return try {
            val credential = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = credential.user
                ?: return AppResult.Failure(AppError.INVALID_CREDENTIALS)
            val snapshot = firestore.collection(USERS).document(firebaseUser.uid).get().await()
            val user = snapshot.toDomainUser() ?: run {
                auth.signOut()
                return AppResult.Failure(AppError.NOT_FOUND)
            }
            _currentUser.value = user
            AppResult.Success(user)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Failure(exception.toFirebaseAppError(), exception)
        }
    }

    override suspend fun signUp(name: String, email: String, password: String): AppResult<User> {
        var createdFirebaseUser: FirebaseUser? = null
        return try {
            val credential = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = credential.user
                ?: return AppResult.Failure(AppError.UNKNOWN)
            createdFirebaseUser = firebaseUser

            val profileRequest = UserProfileChangeRequest.Builder()
                .setDisplayName(name.trim())
                .build()
            firebaseUser.updateProfile(profileRequest).await()

            val user = User(
                id = firebaseUser.uid,
                name = name.trim(),
                email = email.trim().lowercase(),
                createdAt = timeProvider.now(),
            )
            firestore.collection(USERS).document(firebaseUser.uid)
                .set(user.toFirestoreMap()).await()
            _currentUser.value = user
            AppResult.Success(user)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            // Evita deixar uma conta Auth recém-criada sem o documento de perfil correspondente.
            try {
                createdFirebaseUser?.delete()?.await()
            } catch (_: Exception) {
                // O erro original é o mais relevante para a UI; a conta pode exigir limpeza manual.
            }
            AppResult.Failure(exception.toFirebaseAppError(), exception)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AppResult<SocialSignInResult> {
        if (idToken.isBlank()) return AppResult.Failure(AppError.INVALID_CREDENTIALS)
        var createdFirebaseUser: FirebaseUser? = null
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return AppResult.Failure(AppError.UNKNOWN)
            if (result.additionalUserInfo?.isNewUser == true) createdFirebaseUser = firebaseUser

            val reference = firestore.collection(USERS).document(firebaseUser.uid)
            val existing = reference.get().await().toDomainUser()
            if (existing != null) {
                _currentUser.value = existing
                return AppResult.Success(SocialSignInResult(existing, isNewUser = false))
            }

            val email = firebaseUser.email.orEmpty().trim().lowercase()
            val user = User(
                id = firebaseUser.uid,
                name = googleDisplayName(firebaseUser.displayName, email),
                email = email,
                photoUrl = firebaseUser.photoUrl?.toString()?.let(::validPhotoUrlOrNull),
                createdAt = timeProvider.now(),
            )
            reference.set(user.toFirestoreMap()).await()
            _currentUser.value = user
            AppResult.Success(SocialSignInResult(user, isNewUser = true))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            // Conta Auth criada agora sem perfil não deve ficar para trás (igual ao cadastro por e-mail).
            try {
                createdFirebaseUser?.delete()?.await()
            } catch (_: Exception) {
                // O erro original é o mais relevante para a UI.
            }
            AppResult.Failure(exception.toFirebaseAppError(), exception)
        }
    }

    override suspend fun signOut() {
        profileListener?.remove()
        profileListener = null
        auth.signOut()
        _currentUser.value = null
    }

    override suspend fun sendPasswordReset(email: String): AppResult<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            AppResult.Success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Failure(exception.toFirebaseAppError(), exception)
        }
    }

    private companion object {
        const val USERS = "users"
    }
}

/**
 * Nome do perfil criado a partir do Google. As regras do Firestore exigem de 2 a 60
 * caracteres, então nomes ausentes ou curtos demais viram o início do e-mail.
 */
internal fun googleDisplayName(displayName: String?, email: String): String {
    val fromGoogle = displayName.orEmpty().trim().take(60)
    if (fromGoogle.length >= 2) return fromGoogle
    val fromEmail = email.substringBefore('@').trim().take(60)
    return if (fromEmail.length >= 2) fromEmail else "Usuário Sociable"
}

/** As regras só aceitam foto https de até 500 caracteres. */
internal fun validPhotoUrlOrNull(url: String): String? =
    url.takeIf { it.startsWith("https://") && it.length <= 500 }
