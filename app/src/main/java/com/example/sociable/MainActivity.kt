package com.example.sociable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.sociable.navigation.SociableNavHost
import com.example.sociable.ui.theme.SociableTheme
import com.example.sociable.ui.theme.isSociableDarkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SociableTheme {
                // As barras do sistema acompanham o tema escolhido no
                // Perfil → Aparência, inclusive sem recriar a Activity.
                val dark = isSociableDarkTheme()
                SideEffect {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            Color.Transparent.toArgb(),
                            Color.Transparent.toArgb(),
                        ) { dark },
                        navigationBarStyle = SystemBarStyle.auto(
                            LIGHT_SCRIM,
                            DARK_SCRIM,
                        ) { dark },
                    )
                }
                SociableNavHost()
            }
        }
    }

    private companion object {
        // Mesmos scrims usados pelo enableEdgeToEdge padrão.
        val LIGHT_SCRIM = Color(0xE6FFFFFF).toArgb()
        val DARK_SCRIM = Color(0x801B1B1B).toArgb()
    }
}
