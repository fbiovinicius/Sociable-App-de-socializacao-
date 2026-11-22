package com.example.sociable.data.model

/**
 * Participante de um evento, com o mínimo necessário para a prévia no mapa.
 *
 * Só o que é público no app: identificador, nome e foto de perfil.
 * E-mail, bio e demais dados do perfil nunca entram aqui.
 */
data class Attendee(
    val userId: String,
    val name: String,
    val photoUrl: String? = null,
) {
    /** Iniciais para o avatar quando a pessoa não tem foto de perfil. */
    val initials: String
        get() = name.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { part -> part.first().uppercase() }
            .ifBlank { "?" }
}
