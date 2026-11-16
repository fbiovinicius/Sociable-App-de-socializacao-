package com.example.sociable.core.notifications

/**
 * Agenda lembretes locais para eventos confirmados.
 *
 * Contrato:
 *  - [schedule] só agenda se o lembrete ainda fizer sentido ([ReminderPolicy]);
 *  - o mesmo evento tem no máximo um lembrete ativo (agendar de novo substitui);
 *  - [cancel] remove o lembrete quando a pessoa deixa de ir.
 *
 * A implementação atual usa WorkManager (bloco F1). Notificações por push
 * (FCM/Cloud Functions) entram quando o projeto tiver o plano Blaze.
 */
interface ReminderScheduler {

    fun schedule(
        reminder: EventReminder,
        leadMinutes: Long = ReminderPolicy.DEFAULT_LEAD_MINUTES,
    )

    fun cancel(eventId: String)
}
