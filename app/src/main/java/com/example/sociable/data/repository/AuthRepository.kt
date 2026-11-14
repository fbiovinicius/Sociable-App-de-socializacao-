package com.example.sociable.data.repository

import com.example.sociable.core.AppResult
import com.example.sociable.data.model.User
import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato de autenticação.
 *
 * Implementação Firebase esperada:
 *  - signIn  -> FirebaseAuth.signInWithEmailAndPassword
 *  - signUp  -> createUserWithEmailAndPassword + updateProfile(displayName)
 *               + criar doc `users/{uid}` no Firestore
 *  - currentUser -> FirebaseAuth.currentUser + listeners do Firestore
 *
 * O ViewModel nunca conhece Firebase: só esta interface.
 */
interface AuthRepository {

    /** Usuário logado ou `null`. A navegação observa este fluxo. */
    val currentUser: StateFlow<User?>

    suspend fun signIn(email: String, password: String): AppResult<User>

    suspend fun signUp(name: String, email: String, password: String): AppResult<User>

    suspend fun signOut()

    suspend fun sendPasswordReset(email: String): AppResult<Unit>
}
