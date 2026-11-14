package com.example.sociable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.sociable.navigation.SociableNavHost
import com.example.sociable.ui.theme.SociableTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SociableTheme {
                SociableNavHost()
            }
        }
    }
}
