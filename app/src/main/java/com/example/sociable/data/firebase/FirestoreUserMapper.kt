package com.example.sociable.data.firebase

import com.example.sociable.data.model.Interest
import com.example.sociable.data.model.LifeStage
import com.example.sociable.data.model.LookingFor
import com.example.sociable.data.model.SocialVibe
import com.example.sociable.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant
import java.util.Date

internal fun DocumentSnapshot.toDomainUser(): User? {
    if (!exists()) return null
    val createdAt = getTimestamp("createdAt")?.toDate()?.toInstant()
        ?: Instant.now()
    val interestNames = get("interests") as? List<*>
    val interests = interestNames.orEmpty()
        .mapNotNull { value -> (value as? String)?.let { name ->
            runCatching { Interest.valueOf(name) }.getOrNull()
        } }
        .toSet()
    val lookingForNames = get("lookingFor") as? List<*>
    val lookingFor = lookingForNames.orEmpty()
        .mapNotNull { value -> (value as? String)?.let { name ->
            runCatching { LookingFor.valueOf(name) }.getOrNull()
        } }
        .toSet()

    return User(
        id = id,
        name = getString("name") ?: getString("displayName").orEmpty(),
        email = getString("email").orEmpty(),
        bio = getString("bio").orEmpty(),
        photoUrl = getString("photoUrl"),
        instagram = getString("instagram").orEmpty(),
        interests = interests,
        lifeStage = getString("lifeStage")?.let { name ->
            runCatching { LifeStage.valueOf(name) }.getOrNull()
        },
        lookingFor = lookingFor,
        socialVibe = getString("socialVibe")?.let { name ->
            runCatching { SocialVibe.valueOf(name) }.getOrNull()
        },
        createdAt = createdAt,
    )
}

internal fun User.toFirestoreMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "email" to email,
    "bio" to bio,
    "photoUrl" to photoUrl,
    "instagram" to instagram,
    "interests" to interests.map { it.name },
    "lifeStage" to lifeStage?.name,
    "lookingFor" to lookingFor.map { it.name },
    "socialVibe" to socialVibe?.name,
    "createdAt" to Timestamp(Date.from(createdAt)),
)
