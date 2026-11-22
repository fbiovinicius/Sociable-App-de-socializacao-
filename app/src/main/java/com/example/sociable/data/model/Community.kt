package com.example.sociable.data.model

import java.time.Instant

/** Comunidade de bate-papo separada dos eventos. */
data class Community(
    val id: String,
    val name: String,
    val description: String,
    val isPublic: Boolean,
    val creatorId: String,
    val memberCount: Int,
    val createdAt: Instant,
    val icon: String? = null,
    val color: String? = null,
)

/** Participante de uma comunidade. */
data class CommunityMembership(
    val userId: String,
    val communityId: String,
    val joinedAt: Instant,
)

/** Dados para criar uma comunidade. */
data class CommunityDraft(
    val name: String,
    val description: String,
    val isPublic: Boolean,
    val icon: String? = null,
    val color: String? = null,
)