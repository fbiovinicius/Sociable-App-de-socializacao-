package com.example.sociable.data.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sociable.MainActivity
import com.example.sociable.R

/**
 * Mostra a notificação do lembrete (bloco F1).
 *
 * Roda no WorkManager; se o aparelho demorar a acordar (Doze), o lembrete pode
 * chegar alguns minutos atrasado — é o comportamento esperado de WorkManager
 * sem alarmes exatos.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val eventId = inputData.getString(KEY_EVENT_ID) ?: return Result.success()
        val title = inputData.getString(KEY_TITLE).orEmpty()

        // Evento já passou (o aparelho dormiu mais que o previsto): não notifica.
        val startAt = inputData.getLong(KEY_START_AT, 0L)
        if (startAt in 1..System.currentTimeMillis()) {
            return Result.success()
        }

        showNotification(eventId, title)
        return Result.success()
    }

    private fun showNotification(eventId: String, eventTitle: String) {
        val context = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Sem permissão não há como mostrar; falhar silenciosamente é ok.
            return
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            eventId.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Seu rolê é logo ali ⏰")
            .setContentText(eventTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(eventTitle))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(eventId.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_REMINDERS = "event_reminders"
        const val KEY_EVENT_ID = "eventId"
        const val KEY_TITLE = "title"
        const val KEY_START_AT = "startAtMillis"
        const val TAG_REMINDER = "event-reminder"
    }
}
