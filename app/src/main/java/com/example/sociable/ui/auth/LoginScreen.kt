package com.example.sociable.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.feature.auth.LoginViewModel
import com.example.sociable.ui.common.BackLink
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.ScreenTitle
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableTextField
import com.example.sociable.ui.common.message
import com.example.sociable.ui.theme.Plum
import com.example.sociable.ui.theme.SCream
import com.example.sociable.ui.theme.SInk

private val EmailKeyboard = KeyboardOptions(
    keyboardType = KeyboardType.Email,
    capitalization = KeyboardCapitalization.None,
)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoggedIn: () -> Unit,
    onCreateAccount: () -> Unit,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoggedIn()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SCream)
            .systemBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        BackLink(onBack)
        Spacer(modifier = Modifier.height(12.dp))
        ScreenTitle("Entrar com e-mail", sub = "Use seu e-mail e sua senha")

        SociableTextField(
            label = "E-mail",
            value = state.email,
            onChange = viewModel::onEmailChange,
            isError = state.emailError != null,
            keyboardOptions = EmailKeyboard,
            supportingText = state.emailError?.message(),
        )
        Spacer(modifier = Modifier.height(12.dp))
        SociableTextField(
            label = "Senha",
            value = state.password,
            onChange = viewModel::onPasswordChange,
            isError = state.passwordError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                capitalization = KeyboardCapitalization.None,
            ),
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            supportingText = state.passwordError?.message() ?: "Mínimo de 6 caracteres",
        )
        Text(
            text = if (showPassword) "Ocultar senha" else "Mostrar senha",
            color = Plum,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable { showPassword = !showPassword }
                .padding(vertical = 8.dp),
        )
        Text(
            text = "Esqueci minha senha",
            color = SInk,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable(enabled = !state.isLoading, onClick = viewModel::onForgotPasswordClick)
                .padding(vertical = 6.dp),
        )

        if (state.resetEmailSent) {
            Text(
                text = "Enviamos um link de recuperação para o seu e-mail.",
                color = SInk,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        ErrorText(error = state.error)

        Spacer(modifier = Modifier.height(20.dp))
        SociableButton(
            text = "Entrar",
            onClick = viewModel::onLoginClick,
            isLoading = state.isLoading,
        )
        Spacer(modifier = Modifier.height(14.dp))
        SociableButton(
            text = "Criar conta",
            onClick = onCreateAccount,
            secondary = true,
        )
        Text(
            text = "Conta de demonstração: demo@sociable.app / 123456",
            color = Plum,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 24.dp),
        )
    }
}
