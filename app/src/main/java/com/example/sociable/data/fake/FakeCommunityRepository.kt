package com.example.sociable.data.fake

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.data.model.Community
import com.example.sociable.data.model.CommunityDraft
import com.example.sociable.data.model.CommunityMembership
import com.example.sociable.data.repository.CommunityRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Comunidades guardadas em memória (usado nos testes e no modo fake). */
class FakeCommunityRepository(
    private val db: FakeDatabase,
    private val timeProvider: TimeProvider,
) : CommunityRepository {

    override fun observeCommunities(): Flow<List<Community>> =
        db.communities.map { it.filter { it.isPublic } }

    override fun observeMyCommunities(userId: String): Flow<List<Community>> =
        combine(db.communities, db.communityMemberships) { communities, memberships ->
            val myCommunityIds = memberships.entries
                .filter { it.value.contains(userId) }
                .map { it.key }
            communities.filter { it.id in myCommunityIds }
        }

    override fun observeCommunityMembers(communityId: String): Flow<List<CommunityMembership>> =
        combine(db.communityMemberships, db.users) { memberships, users ->
            val memberIds = memberships[communityId].orEmpty()
            memberIds.mapNotNull { userId ->
                users.firstOrNull { it.id == userId }?.let { user ->
                    CommunityMembership(
                        userId = user.id,
                        communityId = communityId,
                        joinedAt = timeProvider.now(),
                    )
                }
            }
        }

    override suspend fun joinCommunity(communityId: String, userId: String): AppResult<Unit> {
        delay(300)
        val community = db.communities.value.firstOrNull { it.id == communityId }
            ?: return AppResult.Failure(AppError.NOT_FOUND)

        if (!community.isPublic) {
            return AppResult.Failure(AppError.PERMISSION_DENIED)
        }

        val current = db.communityMemberships.value[communityId].orEmpty()
        if (userId in current) {
            return AppResult.Success(Unit) // Já é membro
        }

        val updated = current + userId
        db.communityMemberships.update { it + (communityId to updated) }
        db.communities.update { communities ->
            communities.map {
                if (it.id == communityId) it.copy(memberCount = it.memberCount + 1) else it
            }
        }
        return AppResult.Success(Unit)
    }

    override suspend fun createCommunity(
        draft: CommunityDraft,
        creatorId: String,
    ): AppResult<Community> {
        delay(400)
        if (draft.name.isBlank()) {
            return AppResult.Failure(AppError.INVALID_INPUT)
        }

        val community = Community(
            id = "community-${db.communities.value.size + 1}-${timeProvider.now().toEpochMilli()}",
            name = draft.name.trim(),
            description = draft.description.trim(),
            isPublic = draft.isPublic,
            creatorId = creatorId,
            memberCount = 1,
            createdAt = timeProvider.now(),
            icon = draft.icon,
            color = draft.color,
        )

        db.communities.update { it + community }
        db.communityMemberships.update {
            it + (community.id to setOf(creatorId))
        }
        return AppResult.Success(community)
    }
}