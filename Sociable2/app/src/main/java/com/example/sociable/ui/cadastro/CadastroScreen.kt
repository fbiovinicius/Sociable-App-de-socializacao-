
package com.example.sociable.ui.cadastro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

private val Vinho = Color(0xFF8C294C)

@Composable
fun CadastroScreen(
    onCriarConta: (String, String, String) -> Unit,
    onEntrar: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "sociable",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Vinho
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Descubra o que está rolando ao seu redor",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Crie sua conta",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome completo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            visualTransformation =
                PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmarSenha,
            onValueChange = { confirmarSenha = it },
            label = { Text("Confirmar senha") },
            visualTransformation =
                PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (erro.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = erro,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                erro = when {
                    nome.isBlank() ->
                        "Informe seu nome."
                    !android.util.Patterns.EMAIL_ADDRESS
                        .matcher(email.trim()).matches() ->
                        "Informe um e-mail válido."
                    senha.length < 8 ->
                        "A senha deve ter pelo menos 8 caracteres."
                    senha != confirmarSenha ->
                        "As senhas não coincidem."
                    else -> ""
                }

                if (erro.isEmpty()) {
                    onCriarConta(
                        nome.trim(),
                        email.trim(),
                        senha
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Vinho
            )
        ) {
            Text("Criar conta")
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onEntrar) {
            Text("Já tem uma conta? Entrar")
        }
    }
}
