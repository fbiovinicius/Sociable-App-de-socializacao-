package com.example.sociable.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.feature.auth.RegisterViewModel
import com.example.sociable.ui.common.BackLink
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.FormTextField
import com.example.sociable.ui.common.ScreenTitle
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.StickerBox
import com.example.sociable.ui.theme.OrangeSoft
import com.example.sociable.ui.theme.Plum
import com.example.sociable.ui.theme.SCream
import com.example.sociable.ui.theme.SInk

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegistered: () -> Unit,
    onLoginClick: () -> Unit,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) onRegistered()
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
        Spacer(modifier = Modifier.height(8.dp))
        ScreenTitle("Quem é você?", sub = "Só o básico pra começar")

        StickerBox(
            modifier = Modifier.fillMaxWidth(),
            fill = OrangeSoft,
            radius = 14.dp,
            contentPadding = PaddingValues(14.dp),
        ) {
            Text(
                text = "✦ O Sociable é só para maiores de 18 anos.",
                color = SInk,
                fontSize = 13.sp,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        FormTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = "Como quer ser chamado(a)?",
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
            label = "Senha",
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

        Spacer(modifier = Modifier.height(24.dp))
        SociableButton(
            text = "Criar conta",
            onClick = viewModel::onRegisterClick,
            isLoading = state.isLoading,
        )
        Text(
            text = "Já tenho conta",
            color = Plum,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onLoginClick)
                .padding(top = 18.dp),
        )
    }
}
