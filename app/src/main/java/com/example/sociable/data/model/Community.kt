package com.example.sociable.data.model

import java.time.Instant

/** Comunidade criada por uma pessoa: um grupo de conversa aberto a quem quiser entrar. */
data class Community(
    val id: String,
    val name: String,
    val description: String,
    val ownerId: String,
    val ownerName: String,
    val memberCount: Int,
    val createdAt: Instant,
)
