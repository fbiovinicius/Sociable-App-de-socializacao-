package com.example.sociable.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.feature.auth.RegisterViewModel
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.FormTextField
import com.example.sociable.ui.common.PrimaryButton
import com.example.sociable.ui.common.SociableTopBar

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegistered: () -> Unit,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) onRegistered()
    }

    Scaffold(
        topBar = { SociableTopBar(title = "Criar conta", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Leva menos de um minuto para começar a encontrar rolês.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 20.dp),
            )

            FormTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = "Nome",
                error = state.nameError,
            )
            Spacer(modifier = Modifier.height(12.dp))

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
                label = "Senha (mínimo 6 caracteres)",
                error = state.passwordError,
                keyboardType = KeyboardType.Password,
                isPassword = true,
            )
            Spacer(modifier = Modifier.height(12.dp))

            FormTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = "Confirmar senha",
                error = state.confirmPasswordError,
                keyboardType = KeyboardType.Password,
                isPassword = true,
            )

            ErrorText(error = state.error)

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                text = "Criar conta",
                onClick = viewModel::onRegisterClick,
                isLoading = state.isLoading,
            )
        }
    }
}
