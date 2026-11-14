package com.example.sociable.data.model

/**
 * Estado de presença de um usuário em um evento.
 * `isGoing` é sempre relativo ao usuário consultado.
 */
data class RsvpInfo(
    val goingCount: Int = 0,
    val isGoing: Boolean = false,
    val capacity: Int? = null,
) {
    val isFull: Boolean
        get() = capacity != null && goingCount >= capacity
}
