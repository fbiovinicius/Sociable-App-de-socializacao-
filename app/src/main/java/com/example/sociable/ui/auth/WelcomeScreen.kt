package com.example.sociable.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sociable.ui.common.PrimaryButton
import com.example.sociable.ui.common.SociableLogo
import com.example.sociable.ui.theme.WineDark

@Composable
fun WelcomeScreen(
    onLogin: () -> Unit,
    onCreateAccount: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.weight(0.7f))

        SociableLogo()

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SOCIABLE",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = WineDark,
            letterSpacing = 3.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Encontre pessoas para curtir um rolê sem compromisso",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = { /* TODO(backend): integrar Google Sign-In */ },
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(text = "Cadastre-se com o Google (em breve)")
        }

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryButton(text = "Entrar na sua conta", onClick = onLogin)

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onCreateAccount) {
            Text(text = "Criar minha conta com e-mail")
        }

        Text(
            text = "Ao continuar, você concorda com os Termos de Uso e a Política de Privacidade.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
