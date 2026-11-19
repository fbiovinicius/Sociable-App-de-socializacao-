package com.example.sociable.data.repository

import com.example.sociable.core.AppResult
import com.example.sociable.data.model.Interest
import com.example.sociable.data.model.LifeStage
import com.example.sociable.data.model.LookingFor
import com.example.sociable.data.model.SocialVibe
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

    /**
     * Atualiza as respostas do onboarding social, preservando os demais campos
     * do perfil. [lifeStage] e [socialVibe] nulos significam etapa pulada.
     */
    suspend fun updateSocialProfile(
        userId: String,
        lifeStage: LifeStage?,
        lookingFor: Set<LookingFor>,
        socialVibe: SocialVibe?,
    ): AppResult<User>
}
