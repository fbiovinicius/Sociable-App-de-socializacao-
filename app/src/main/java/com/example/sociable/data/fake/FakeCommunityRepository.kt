package com.example.sociable.data.fake

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.model.Community
import com.example.sociable.data.model.Message
import com.example.sociable.data.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Comunidades em memória, para os testes. */
class FakeCommunityRepository(
    private val timeProvider: TimeProvider,
) : CommunityRepository {

    private val communities = MutableStateFlow<List<Community>>(emptyList())

    /** communityId -> ids dos membros. */
    private val members = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    private val messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    private var nextId = 1

    override fun observeCommunities(): Flow<List<Community>> =
        communities.map { list -> list.sortedByDescending { it.memberCount } }

    override fun observeCommunity(communityId: String): Flow<Community?> =
        communities.map { list -> list.firstOrNull { it.id == communityId } }

    override fun observeMemberships(userId: String): Flow<Set<String>> =
        members.map { all -> all.filterValues { userId in it }.keys }

    override suspend fun createCommunity(
        name: String,
        description: String,
        ownerId: String,
        ownerName: String,
    ): AppResult<Community> {
        if (Validators.communityName(name) != null ||
            Validators.communityDescription(description) != null
        ) {
            return AppResult.Failure(AppError.INVALID_INPUT)
        }
        val community = Community(
            id = "community-${nextId++}",
            name = name.trim(),
            description = description.trim(),
            ownerId = ownerId,
            ownerName = ownerName,
            memberCount = 1,
            createdAt = timeProvider.now(),
        )
        communities.update { it + community }
        members.update { it + (community.id to setOf(ownerId)) }
        return AppResult.Success(community)
    }

    override suspend fun join(communityId: String, userId: String, userName: String): AppResult<Unit> {
        if (communities.value.none { it.id == communityId }) {
            return AppResult.Failure(AppError.NOT_FOUND)
        }
        if (userId in members.value[communityId].orEmpty()) return AppResult.Success(Unit)
        members.update { it + (communityId to it[communityId].orEmpty() + userId) }
        updateCount(communityId, +1)
        return AppResult.Success(Unit)
    }

    override suspend fun leave(communityId: String, userId: String): AppResult<Unit> {
        val community = communities.value.firstOrNull { it.id == communityId }
            ?: return AppResult.Failure(AppError.NOT_FOUND)
        if (community.ownerId == userId) return AppResult.Failure(AppError.PERMISSION_DENIED)
        if (userId !in members.value[communityId].orEmpty()) return AppResult.Success(Unit)
        members.update { it + (communityId to it[communityId].orEmpty() - userId) }
        updateCount(communityId, -1)
        return AppResult.Success(Unit)
    }

    override fun observeMessages(communityId: String): Flow<List<Message>> =
        messages.map { all -> all[communityId].orEmpty().sortedBy { it.sentAt } }

    override fun observeLastMessage(communityId: String): Flow<Message?> =
        messages.map { all -> all[communityId].orEmpty().maxByOrNull { it.sentAt } }

    override suspend fun sendMessage(
        communityId: String,
        authorId: String,
        authorName: String,
        text: String,
    ): AppResult<Message> {
        if (authorId !in members.value[communityId].orEmpty()) {
            return AppResult.Failure(AppError.PERMISSION_DENIED)
        }
        val trimmed = text.trim()
        if (Validators.message(trimmed) != null) return AppResult.Failure(AppError.INVALID_INPUT)
        val message = Message(
            id = "cmessage-${nextId++}",
            eventId = communityId,
            authorId = authorId,
            authorName = authorName,
            text = trimmed,
            sentAt = timeProvider.now(),
        )
        messages.update { it + (communityId to it[communityId].orEmpty() + message) }
        return AppResult.Success(message)
    }

    private fun updateCount(communityId: String, delta: Int) {
        communities.update { list ->
            list.map { if (it.id == communityId) it.copy(memberCount = it.memberCount + delta) else it }
        }
    }
}
