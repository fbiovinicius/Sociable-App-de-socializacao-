package com.example.sociable.data.model

import java.time.Instant

/**
 * Usuário do app. É o contrato que o backend (Firestore) deve espelhar.
 * A senha NUNCA faz parte do modelo: ela fica só no Firebase Auth.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val bio: String = "",
    val photoUrl: String? = null,
    val interests: Set<Interest> = emptySet(),
    val createdAt: Instant,
)
