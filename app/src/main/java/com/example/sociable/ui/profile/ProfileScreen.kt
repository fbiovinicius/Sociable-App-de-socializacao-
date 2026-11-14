@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sociable.ui.profile


import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.data.model.Interest
import com.example.sociable.feature.profile.ProfileViewModel
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.FormTextField
import com.example.sociable.ui.common.InfoRow
import com.example.sociable.ui.common.PrimaryButton
import com.example.sociable.ui.common.SecondaryButton
import com.example.sociable.ui.common.SectionTitle
import com.example.sociable.ui.common.SociableTopBar

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(state.isSignedOut) {
        if (state.isSignedOut) onSignedOut()
    }

    Scaffold(
        topBar = { SociableTopBar(title = "Meu perfil", onBack = onBack) },
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            InfoRow(label = "E-mail", value = state.email)

            FormTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = "Nome",
                error = state.nameError,
            )

            FormTextField(
                value = state.bio,
                onValueChange = viewModel::onBioChange,
                label = "Bio",
                error = state.bioError,
                singleLine = false,
                minLines = 3,
            )

            FormTextField(
                value = state.photoUrl,
                onValueChange = viewModel::onPhotoUrlChange,
                label = "URL da foto (opcional)",
                error = state.photoUrlError,
                keyboardType = KeyboardType.Uri,
            )

            SectionTitle(text = "Interesses", modifier = Modifier.padding(top = 8.dp))

            Text(
                text = "Escolha os temas que combinam com você. Usamos isso para sugerir eventos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Interest.entries.forEach { interest ->
                    FilterChip(
                        selected = interest in state.interests,
                        onClick = { viewModel.onInterestToggle(interest) },
                        label = { Text(interest.label) },
                    )
                }
            }

            ErrorText(error = state.error)

            if (state.isSaved) {
                Text(
                    text = "Perfil atualizado com sucesso!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryButton(
                text = "Salvar alterações",
                onClick = viewModel::onSaveClick,
                isLoading = state.isSaving,
            )

            SecondaryButton(text = "Sair da conta", onClick = viewModel::onSignOutClick)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
