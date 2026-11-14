package com.example.sociable.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.R
import com.example.sociable.data.model.Interest
import com.example.sociable.feature.profile.ProfileViewModel
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.FormTextField
import com.example.sociable.ui.common.Pin
import com.example.sociable.ui.common.ScreenTitle
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.theme.Plum
import com.example.sociable.ui.theme.SCream
import com.example.sociable.ui.theme.SInk

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onSignedOut: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(state.isSignedOut) {
        if (state.isSignedOut) onSignedOut()
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SCream),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = SInk)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SCream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 28.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Pin(R.drawable.ic_pin_party, size = 88.dp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = state.name.ifBlank { "Meu perfil" },
            color = SInk,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = state.email,
            color = Plum,
            fontSize = 13.sp,
        )

        Spacer(modifier = Modifier.height(24.dp))
        ScreenTitle("Editar perfil", sub = "Como as pessoas vão te ver")

        FormTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = "Nome",
            error = state.nameError,
        )
        Spacer(modifier = Modifier.height(12.dp))
        FormTextField(
            value = state.bio,
            onValueChange = viewModel::onBioChange,
            label = "Bio",
            error = state.bioError,
            singleLine = false,
            minLines = 3,
        )
        Spacer(modifier = Modifier.height(12.dp))
        FormTextField(
            value = state.photoUrl,
            onValueChange = viewModel::onPhotoUrlChange,
            label = "URL da foto (opcional)",
            error = state.photoUrlError,
            keyboardType = KeyboardType.Uri,
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Meus interesses",
            color = SInk,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
        )
        Text(
            text = "Usamos isso para sugerir rolês com a sua cara.",
            color = Plum,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Interest.entries.forEach { interest ->
                SociableChip(
                    text = interest.label,
                    selected = interest in state.interests,
                    onClick = { viewModel.onInterestToggle(interest) },
                )
            }
        }

        ErrorText(error = state.error)
        if (state.isSaved) {
            Text(
                text = "Perfil atualizado com sucesso!",
                color = SInk,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        SociableButton(
            text = "Salvar alterações",
            onClick = viewModel::onSaveClick,
            isLoading = state.isSaving,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SociableButton(
            text = "Sair da conta",
            onClick = viewModel::onSignOutClick,
            secondary = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
