package com.example.sociable.data.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.sociable.core.notifications.EventReminder
import com.example.sociable.core.notifications.ReminderPolicy
import com.example.sociable.core.notifications.ReminderScheduler
import java.util.concurrent.TimeUnit

/**
 * Lembretes locais via WorkManager (bloco F1).
 *
 * - Um trabalho único por evento (`reminder-<eventId>`): reagendar substitui;
 * - `cancel` remove o trabalho quando a pessoa cancela a presença;
 * - o WorkManager persiste os trabalhos: sobrevive a fechar o app e reagenda
 *   sozinho depois de reiniciar o aparelho.
 */
class WorkManagerReminderScheduler(
    private val context: Context,
) : ReminderScheduler {

    override fun schedule(
        reminder: EventReminder,
        leadMinutes: Long,
    ) {
        val delayMillis = ReminderPolicy.delayMillis(
            startAtMillis = reminder.startAtMillis,
            nowMillis = System.currentTimeMillis(),
            leadMinutes = leadMinutes,
        ) ?: return // evento já começou ou está bem perto: nada a lembrar.

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ReminderWorker.KEY_EVENT_ID to reminder.eventId,
                    ReminderWorker.KEY_TITLE to reminder.title,
                    ReminderWorker.KEY_START_AT to reminder.startAtMillis,
                ),
            )
            .addTag(ReminderWorker.TAG_REMINDER)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName(reminder.eventId), ExistingWorkPolicy.REPLACE, request)
    }

    override fun cancel(eventId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName(eventId))
    }

    private fun uniqueName(eventId: String) = "reminder-$eventId"
}
