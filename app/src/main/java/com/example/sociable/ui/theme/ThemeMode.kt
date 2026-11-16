package com.example.sociable.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Modo de aparência escolhido pela pessoa no perfil (Perfil → Aparência). */
enum class ThemeMode(val label: String) {
    SYSTEM("Sistema"),
    LIGHT("Claro"),
    DARK("Escuro"),
}

/**
 * Guarda o modo de tema e persiste em SharedPreferences.
 *
 * Inicialize uma vez em [com.example.sociable.SociableApplication]; qualquer
 * tela pode ler [mode] (recompõe sozinho quando o modo troca) e alterar com
 * [setMode]. O `Theme.kt` usa isso para decidir a paleta, e o
 * `MapScreen` para escolher o estilo escuro do Google Maps.
 */
object ThemeController {

    private const val PREFS_NAME = "sociable_prefs"
    private const val KEY_MODE = "theme_mode"

    var mode: ThemeMode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    /** Chamado no `Application.onCreate`, antes de qualquer Activity. */
    fun init(context: Context) {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MODE, null)
        mode = stored?.let { value -> ThemeMode.entries.firstOrNull { it.name == value } }
            ?: ThemeMode.SYSTEM
    }

    /** Troca o modo na hora e guarda a escolha para as próximas aberturas. */
    fun setMode(newMode: ThemeMode, context: Context) {
        mode = newMode
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODE, newMode.name)
            .apply()
    }
}
