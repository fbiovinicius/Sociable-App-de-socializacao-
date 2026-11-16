package com.example.sociable

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.sociable.data.notifications.ReminderWorker
import com.example.sociable.di.appModule
import com.example.sociable.ui.theme.ThemeController
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SociableApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Modo de tema (sistema/claro/escuro) escolhido e persistido pela pessoa.
        ThemeController.init(this)
        createNotificationChannels()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@SociableApplication)
            modules(appModule)
        }
    }

    /** Canal dos lembretes locais (bloco F1). Criar uma vez é idempotente. */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            ReminderWorker.CHANNEL_REMINDERS,
            getString(R.string.channel_reminders_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(R.string.channel_reminders_description)
        }

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}
