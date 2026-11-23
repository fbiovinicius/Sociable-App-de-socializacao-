package com.example.sociable.data.repository

import com.example.sociable.core.AppResult
import com.example.sociable.data.model.Community
import com.example.sociable.data.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Comunidades (grupos de conversa abertos) e o chat de cada uma.
 *
 * Implementação Firebase: coleção `communities`, subcoleção `members/{userId}`
 * (quem entrou) e `messages`. `memberCount` é desnormalizado e atualizado no
 * mesmo batch que cria/apaga o documento de membro. As mensagens reutilizam o
 * modelo [Message], com `eventId` carregando o id da comunidade.
 */
interface CommunityRepository {

    /** Todas as comunidades, das mais cheias para as menos cheias. */
    fun observeCommunities(): Flow<List<Community>>

    fun observeCommunity(communityId: String): Flow<Community?>

    /** Ids das comunidades em que a pessoa entrou. */
    fun observeMemberships(userId: String): Flow<Set<String>>

    suspend fun createCommunity(
        name: String,
        description: String,
        ownerId: String,
        ownerName: String,
    ): AppResult<Community>

    suspend fun join(communityId: String, userId: String, userName: String): AppResult<Unit>

    /** Sair não é permitido para quem criou a comunidade. */
    suspend fun leave(communityId: String, userId: String): AppResult<Unit>

    fun observeMessages(communityId: String): Flow<List<Message>>

    fun observeLastMessage(communityId: String): Flow<Message?>

    suspend fun sendMessage(
        communityId: String,
        authorId: String,
        authorName: String,
        text: String,
    ): AppResult<Message>
}
