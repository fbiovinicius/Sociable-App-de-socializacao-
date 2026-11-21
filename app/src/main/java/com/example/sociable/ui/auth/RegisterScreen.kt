package com.example.sociable.ui.auth

import androidx.compose.foundation.background
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
import com.example.sociable.ui.common.SociableTextLink
import com.example.sociable.ui.theme.SociableTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import java.time.LocalDate
import com.example.sociable.ui.common.DateMask
import com.example.sociable.ui.common.SociableTextField
import com.example.sociable.ui.common.isAdult
import com.example.sociable.ui.common.parseBirthDate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.text.font.FontWeight
import com.example.sociable.ui.common.SociableChip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegistered: () -> Unit,
    onLoginClick: () -> Unit,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value


    // Data de nascimento: por enquanto só na tela (o backend ainda não guarda).
    // Mesmo assim ela bloqueia o botão "Criar conta" para menores de 18 anos.
    var birthDigits by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf<String?>(null) }
    val birthDate = parseBirthDate(birthDigits)
    val birthError = when {
        birthDigits.length < 8 -> null
        birthDate == null || birthDate.isAfter(LocalDate.now()) ->
            "Digite uma data válida (DD/MM/AAAA)."
        !isAdult(birthDate) -> "O Sociable é só para maiores de 18 anos."
        else -> null
    }
    val isAdultOk = birthDate != null && birthError == null

    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) onRegistered()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SociableTheme.background)
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
            fill = SociableTheme.accentSoft,
            radius = 14.dp,
            contentPadding = PaddingValues(14.dp),
        ) {
            Text(
                text = "✦ O Sociable é só para maiores de 18 anos.",
                color = SociableTheme.ink,
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

        SociableTextField(
            label = "Data de nascimento",
            value = birthDigits,
            onChange = { birthDigits = it.filter(Char::isDigit).take(8) },
            isError = birthError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = DateMask,
            supportingText = birthError
                ?: "Precisamos confirmar que você tem 18 anos ou mais. A data não aparece no seu perfil.",
        )
        Spacer(modifier = Modifier.height(12.dp))

        Spacer(modifier = Modifier.height(12.dp))
        FormTextField(
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = "Confirmar senha",
            error = state.confirmPasswordError,
            keyboardType = KeyboardType.Password,
            isPassword = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sexo",
            color = SociableTheme.ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("Feminino", "Masculino", "Não binário").forEach { option ->
                SociableChip(
                    text = option,
                    selected = gender == option,
                    onClick = { gender = option },
                )
            }
        }
        ErrorText(error = state.error)

        Spacer(modifier = Modifier.height(24.dp))
        SociableButton(
            text = "Criar conta",
            onClick = viewModel::onRegisterClick,
            enabled = isAdultOk,
            isLoading = state.isLoading,
        )
        SociableTextLink(
            text = "Já tenho conta",
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
