package com.example.sociable.core.notifications

/**
 * Lembrete local de um evento (bloco F1).
 *
 * Não carrega nada de Android: o agendador converte isso em trabalho do
 * WorkManager e o worker transforma em notificação.
 */
data class EventReminder(
    val eventId: String,
    val title: String,
    /** Instante de início do evento, em epoch millis. */
    val startAtMillis: Long,
)

/**
 * Regra de quando o lembrete deve disparar. Pura, para os testes
 * determinísticos — o WorkManager só recebe o resultado (o atraso).
 */
object ReminderPolicy {

    /** Lembrete padrão: 1 hora antes do início. */
    const val DEFAULT_LEAD_MINUTES = 60L

    /**
     * Atraso, em millis, entre agora e o momento do lembrete.
     *
     * Retorna `null` quando o lembrete já não faz sentido: o evento começa
     * antes do intervalo escolhido ou já começou.
     */
    fun delayMillis(
        startAtMillis: Long,
        nowMillis: Long,
        leadMinutes: Long = DEFAULT_LEAD_MINUTES,
    ): Long? {
        val triggerAt = startAtMillis - leadMinutes * MILLIS_PER_MINUTE
        if (triggerAt <= nowMillis) return null
        return triggerAt - nowMillis
    }

    const val MILLIS_PER_MINUTE = 60_000L
}
