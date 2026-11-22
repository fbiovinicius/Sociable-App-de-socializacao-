package com.example.sociable.data.repository

import com.example.sociable.core.AppResult
import com.example.sociable.data.model.Community
import com.example.sociable.data.model.CommunityDraft
import com.example.sociable.data.model.CommunityMembership
import kotlinx.coroutines.flow.Flow

/**
 * Comunidades de bate-papo separadas dos eventos.
 *
 * Implementação Firebase esperada:
 *  - coleção `communities` no Firestore;
 *  - subcoleção `communities/{communityId}/members/{userId}` para membros;
 *  - somente comunidades públicas são listadas para quem não é membro;
 *  - comunidades privadas exigem convite ou aprovação do criador.
 */
interface CommunityRepository {

    /** Comunidades públicas disponíveis para qualquer usuário autenticado entrar. */
    fun observeCommunities(): Flow<List<Community>>

    /** Comunidades que o usuário já entrou. */
    fun observeMyCommunities(userId: String): Flow<List<Community>>

    /** Membros de uma comunidade. */
    fun observeCommunityMembers(communityId: String): Flow<List<CommunityMembership>>

    /** Entra em uma comunidade pública. */
    suspend fun joinCommunity(communityId: String, userId: String): AppResult<Unit>

    /** Cria uma nova comunidade. O criador vira automaticamente membro. */
    suspend fun createCommunity(
        draft: CommunityDraft,
        creatorId: String,
    ): AppResult<Community>
}