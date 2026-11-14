package com.example.sociable.data.repository

import com.example.sociable.core.AppResult
import com.example.sociable.data.model.Interest
import com.example.sociable.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Perfil do usuário.
 *
 * Implementação Firebase esperada: documento `users/{userId}` no Firestore,
 * com listener em tempo real (`addSnapshotListener`) para [observeUser].
 */
interface UserRepository {

    fun observeUser(userId: String): Flow<User?>

    suspend fun updateProfile(
        userId: String,
        name: String,
        bio: String,
        photoUrl: String?,
        interests: Set<Interest>,
    ): AppResult<User>
}
