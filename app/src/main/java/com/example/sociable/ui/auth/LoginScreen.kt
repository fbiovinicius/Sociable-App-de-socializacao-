package com.example.sociable.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.feature.auth.LoginViewModel
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.FormTextField
import com.example.sociable.ui.common.PrimaryButton
import com.example.sociable.ui.common.SociableTopBar
import com.example.sociable.ui.common.SociableLogo

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoggedIn: () -> Unit,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoggedIn()
    }

    Scaffold(
        topBar = { SociableTopBar(title = "Entrar", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SociableLogo(size = 88.dp)
            Spacer(modifier = Modifier.height(24.dp))

            FormTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = "E-mail",
                error = state.emailError,
                keyboardType = KeyboardType.Email,
            )
            Spacer(modifier = Modifier.height(12.dp))

            FormTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Senha",
                error = state.passwordError,
                keyboardType = KeyboardType.Password,
                isPassword = true,
            )

            ErrorText(error = state.error)

            if (state.resetEmailSent) {
                Text(
                    text = "Enviamos um link de recuperação para o seu e-mail.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                text = "Entrar",
                onClick = viewModel::onLoginClick,
                isLoading = state.isLoading,
            )

            TextButton(
                onClick = viewModel::onForgotPasswordClick,
                enabled = !state.isLoading,
            ) {
                Text(text = "Esqueci minha senha")
            }

            Text(
                text = "Conta de demonstração: demo@sociable.app / 123456",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}
