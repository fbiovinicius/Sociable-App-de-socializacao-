package com.example.sociable.data.firebase

import com.example.sociable.data.model.Community

/** Nomes dos campos da coleção `communities`. */
internal object CommunityFields {
    const val NAME = "name"
    const val DESCRIPTION = "description"
    const val OWNER_ID = "ownerId"
    const val OWNER_NAME = "ownerName"
    const val MEMBER_COUNT = "memberCount"
    const val CREATED_AT = "createdAt"
}

/** Nomes dos campos de `communities/{id}/members/{userId}`. */
internal object CommunityMemberFields {
    const val USER_ID = "userId"
    const val NAME = "name"
    const val JOINED_AT = "joinedAt"
}

/**
 * Conversão pura de um documento de comunidade. Devolve `null` quando faltam
 * nome, dono ou data, para um documento malformado não derrubar a lista.
 */
internal fun mapToCommunity(id: String, data: Map<String, Any?>): Community? {
    val name = data[CommunityFields.NAME] as? String ?: return null
    val ownerId = data[CommunityFields.OWNER_ID] as? String ?: return null
    val createdAt = data[CommunityFields.CREATED_AT].toInstantOrNull() ?: return null
    return Community(
        id = id,
        name = name,
        description = data[CommunityFields.DESCRIPTION] as? String ?: "",
        ownerId = ownerId,
        ownerName = data[CommunityFields.OWNER_NAME] as? String ?: "",
        memberCount = ((data[CommunityFields.MEMBER_COUNT] as? Number)?.toInt() ?: 0).coerceAtLeast(0),
        createdAt = createdAt,
    )
}
