package com.example.sociable.data.firebase

import com.example.sociable.core.AppError
import com.example.sociable.core.AppResult
import com.example.sociable.core.time.TimeProvider
import com.example.sociable.core.validation.Validators
import com.example.sociable.data.model.Community
import com.example.sociable.data.model.Message
import com.example.sociable.data.repository.CommunityRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Comunidades em `communities/{id}`, com `members/{userId}` e `messages`.
 *
 * Criar, entrar e sair são `WriteBatch`: o documento de membro e o contador
 * `memberCount` mudam juntos (as regras exigem isso). A lista de comunidades
 * da pessoa vem de uma consulta de grupo de coleções em `members`, que precisa
 * da exceção de índice descrita no README.
 */
class FirestoreCommunityRepository(
    private val firestore: FirebaseFirestore,
    private val timeProvider: TimeProvider,
) : CommunityRepository {

    override fun observeCommunities(): Flow<List<Community>> =
        communities.orderBy(CommunityFields.CREATED_AT, Query.Direction.DESCENDING)
            .limit(COMMUNITY_LIMIT)
            .snapshotFlow()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { it.toDomainCommunity() }
                    .sortedWith(compareByDescending<Community> { it.memberCount }.thenBy { it.name })
            }

    override fun observeCommunity(communityId: String): Flow<Community?> =
        communities.document(communityId).snapshotFlow().map { it.toDomainCommunity() }

    override fun observeMemberships(userId: String): Flow<Set<String>> =
        firestore.collectionGroup(SUBCOLLECTION_MEMBERS)
            .whereEqualTo(CommunityMemberFields.USER_ID, userId)
            .snapshotFlow()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.reference.parent.parent?.id }.toSet()
            }

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
        return try {
            val reference = communities.document()
            val cleanName = name.trim()
            val cleanDescription = description.trim()
            firestore.batch()
                .set(
                    reference,
                    mapOf(
                        CommunityFields.NAME to cleanName,
                        CommunityFields.DESCRIPTION to cleanDescription,
                        CommunityFields.OWNER_ID to ownerId,
                        CommunityFields.OWNER_NAME to ownerName.take(AUTHOR_NAME_MAX),
                        CommunityFields.MEMBER_COUNT to 1,
                        CommunityFields.CREATED_AT to FieldValue.serverTimestamp(),
                    ),
                )
                .set(reference.collection(SUBCOLLECTION_MEMBERS).document(ownerId), memberMap(ownerId, ownerName))
                .commit()
                .await()
            AppResult.Success(
                Community(
                    id = reference.id,
                    name = cleanName,
                    description = cleanDescription,
                    ownerId = ownerId,
                    ownerName = ownerName,
                    memberCount = 1,
                    createdAt = timeProvider.now(),
                )
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Failure(exception.toFirebaseAppError(), exception)
        }
    }

    override suspend fun join(communityId: String, userId: String, userName: String): AppResult<Unit> =
        try {
            val community = communities.document(communityId)
            val member = community.collection(SUBCOLLECTION_MEMBERS).document(userId)
            if (member.get().await().exists()) {
                AppResult.Success(Unit)
            } else {
                firestore.batch()
                    .set(member, memberMap(userId, userName))
                    .update(community, CommunityFields.MEMBER_COUNT, FieldValue.increment(1))
                    .commit()
                    .await()
                AppResult.Success(Unit)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Failure(exception.toFirebaseAppError(), exception)
        }

    override suspend fun leave(communityId: String, userId: String): AppResult<Unit> =
        try {
            val community = communities.document(communityId)
            val member = community.collection(SUBCOLLECTION_MEMBERS).document(userId)
            val ownerId = community.get().await().getString(CommunityFields.OWNER_ID)
            if (ownerId == userId) {
                AppResult.Failure(AppError.PERMISSION_DENIED)
            } else if (!member.get().await().exists()) {
                AppResult.Success(Unit)
            } else {
                firestore.batch()
                    .delete(member)
                    .update(community, CommunityFields.MEMBER_COUNT, FieldValue.increment(-1))
                    .commit()
                    .await()
                AppResult.Success(Unit)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Failure(exception.toFirebaseAppError(), exception)
        }

    override fun observeMessages(communityId: String): Flow<List<Message>> =
        messages(communityId)
            .orderBy(MessageFields.SENT_AT, Query.Direction.ASCENDING)
            .limitToLast(MESSAGE_LIMIT)
            .snapshotFlow()
            .map { snapshot -> snapshot.documents.mapNotNull { it.toDomainMessage(communityId) } }

    override fun observeLastMessage(communityId: String): Flow<Message?> =
        messages(communityId)
            .orderBy(MessageFields.SENT_AT, Query.Direction.DESCENDING)
            .limit(1)
            .snapshotFlow()
            .map { snapshot -> snapshot.documents.firstNotNullOfOrNull { it.toDomainMessage(communityId) } }

    override suspend fun sendMessage(
        communityId: String,
        authorId: String,
        authorName: String,
        text: String,
    ): AppResult<Message> {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || trimmed.length > Validators.MESSAGE_MAX) {
            return AppResult.Failure(AppError.INVALID_INPUT)
        }
        return try {
            val reference = messages(communityId).document()
            reference.set(
                mapOf(
                    MessageFields.AUTHOR_ID to authorId,
                    MessageFields.AUTHOR_NAME to authorName.take(AUTHOR_NAME_MAX),
                    MessageFields.TEXT to trimmed,
                    MessageFields.SENT_AT to FieldValue.serverTimestamp(),
                )
            ).await()
            AppResult.Success(
                Message(
                    id = reference.id,
                    eventId = communityId,
                    authorId = authorId,
                    authorName = authorName,
                    text = trimmed,
                    sentAt = timeProvider.now(),
                )
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Failure(exception.toFirebaseAppError(), exception)
        }
    }

    private val communities
        get() = firestore.collection(COLLECTION_COMMUNITIES)

    private fun messages(communityId: String) =
        communities.document(communityId).collection(SUBCOLLECTION_MESSAGES)

    private fun memberMap(userId: String, name: String): Map<String, Any> = mapOf(
        CommunityMemberFields.USER_ID to userId,
        CommunityMemberFields.NAME to name.take(AUTHOR_NAME_MAX),
        CommunityMemberFields.JOINED_AT to FieldValue.serverTimestamp(),
    )

    private fun DocumentSnapshot.toDomainCommunity(): Community? =
        getData(DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?.let { data -> mapToCommunity(id = id, data = data) }

    private fun DocumentSnapshot.toDomainMessage(communityId: String): Message? =
        getData(DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?.let { data -> mapToMessage(id = id, eventId = communityId, data = data) }

    private companion object {
        const val COLLECTION_COMMUNITIES = "communities"
        const val SUBCOLLECTION_MEMBERS = "members"
        const val SUBCOLLECTION_MESSAGES = "messages"
        const val COMMUNITY_LIMIT = 100L
        const val MESSAGE_LIMIT = 200L
        const val AUTHOR_NAME_MAX = 100
    }
}
