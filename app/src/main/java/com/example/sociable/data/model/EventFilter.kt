package com.example.sociable.data.model

import com.example.sociable.core.time.ManausTime
import java.time.Instant

/**
 * Filtro do feed. O backend traduz isso para uma query do Firestore.
 * O método [matches] é a implementação de referência (usada pelos fakes e testes).
 */
data class EventFilter(
    val query: String = "",
    val categories: Set<EventCategory> = emptySet(),
    val dateFilter: DateFilter = DateFilter.ALL,
    val onlyFree: Boolean = false,
    /** Mostra apenas eventos marcados como "segredinho". */
    val onlySecret: Boolean = false,
) {
    val isEmpty: Boolean
        get() = query.isBlank() &&
            categories.isEmpty() &&
            dateFilter == DateFilter.ALL &&
            !onlyFree &&
            !onlySecret
}

enum class DateFilter(val label: String) {
    ALL("Todos"),
    TODAY("Hoje"),
    TOMORROW("Amanhã"),
    NEXT_7_DAYS("Próximos 7 dias"),
}

/**
 * Aplica o filtro em memória. `now` é injetado para o teste não depender do relógio.
 */
fun EventFilter.matches(event: Event, now: Instant): Boolean {
    if (query.isNotBlank()) {
        val term = query.trim()
        // O local de um segredinho não entra na busca para não vazar o endereço.
        val matchesPlace = !event.isSecret &&
            (event.venueName.contains(term, ignoreCase = true) ||
                event.address.contains(term, ignoreCase = true))
        val matchesText = event.title.contains(term, ignoreCase = true) || matchesPlace
        if (!matchesText) return false
    }

    if (categories.isNotEmpty() && event.category !in categories) return false
    if (onlyFree && !event.isFree) return false
    if (onlySecret && !event.isSecret) return false

    val eventDate = event.startAt.atZone(ManausTime.ZONE).toLocalDate()
    val today = now.atZone(ManausTime.ZONE).toLocalDate()
    return when (dateFilter) {
        DateFilter.ALL -> true
        DateFilter.TODAY -> eventDate == today
        DateFilter.TOMORROW -> eventDate == today.plusDays(1)
        DateFilter.NEXT_7_DAYS ->
            !eventDate.isBefore(today) && !eventDate.isAfter(today.plusDays(7))
    }
}
