package com.example.sociable.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sociable.ui.common.SociableTextLink
import androidx.compose.ui.unit.sp
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableLogo
import com.example.sociable.ui.theme.SociableTheme

@Composable
fun WelcomeScreen(
    onLogin: () -> Unit,
    onCreateAccount: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SociableTheme.background)
            .systemBarsPadding()
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SociableLogo(size = 220.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Rolê bom é rolê com gente nova.",
                color = SociableTheme.muted,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
        }

        SociableButton(text = "Entrar com e-mail", onClick = onLogin)
        Spacer(modifier = Modifier.height(14.dp))
        SociableButton(
            text = "Criar minha conta",
            onClick = onCreateAccount,
            secondary = true,
        )
        Text(
            text = "Só para maiores de 18 anos.",
            color = SociableTheme.muted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
        )
    }
}
