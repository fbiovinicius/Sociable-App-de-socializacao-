package com.example.sociable.data.firebase

import com.example.sociable.data.model.Interest
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

    return User(
        id = id,
        name = getString("name") ?: getString("displayName").orEmpty(),
        email = getString("email").orEmpty(),
        bio = getString("bio").orEmpty(),
        photoUrl = getString("photoUrl"),
        interests = interests,
        createdAt = createdAt,
    )
}

internal fun User.toFirestoreMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "email" to email,
    "bio" to bio,
    "photoUrl" to photoUrl,
    "interests" to interests.map { it.name },
    "createdAt" to Timestamp(Date.from(createdAt)),
)
