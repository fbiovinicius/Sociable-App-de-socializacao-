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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.data.model.Interest
import com.example.sociable.data.model.LifeStage
import com.example.sociable.data.model.LookingFor
import com.example.sociable.data.model.LookingForRules
import com.example.sociable.data.model.SocialVibe
import com.example.sociable.feature.profile.ProfileViewModel
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.FormTextField
import com.example.sociable.ui.common.ProfilePhotoOrPin
import com.example.sociable.ui.common.ProgressRingAvatar
import com.example.sociable.ui.common.ScreenTitle
import com.example.sociable.ui.common.SectionTitle
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.theme.SociableTheme
import com.example.sociable.ui.theme.ThemeController
import com.example.sociable.ui.theme.ThemeMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onSignedOut: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    // Os botões do topo rolam até o campo e abrem o teclado nele.
    val photoFocus = remember { FocusRequester() }
    val instagramFocus = remember { FocusRequester() }
    var photoFieldY by remember { mutableFloatStateOf(0f) }
    var instagramFieldY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(state.isSignedOut) {
        if (state.isSignedOut) onSignedOut()
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SociableTheme.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = SociableTheme.ink)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SociableTheme.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(start = 24.dp, end = 28.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        ProgressRingAvatar(progress = state.completion, diameter = 124.dp) {
            // Só a foto já salva: digitar a URL não dispara download a cada letra.
            ProfilePhotoOrPin(photoUrl = state.savedPhotoUrl)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = state.name.ifBlank { "Meu perfil" },
            color = SociableTheme.ink,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = state.email,
            color = SociableTheme.muted,
            fontSize = 13.sp,
        )

        // Atalhos para o que ainda falta no perfil (somem quando preenchido).
        if (state.instagram.isBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            SociableButton(
                text = "@ Adicionar redes sociais",
                onClick = {
                    scope.launch {
                        scrollState.animateScrollTo(instagramFieldY.toInt())
                        instagramFocus.requestFocus()
                    }
                },
                secondary = true,
            )
        }
        if (state.photoUrl.isBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            SociableButton(
                text = "Adicionar sua foto de perfil",
                onClick = {
                    scope.launch {
                        scrollState.animateScrollTo(photoFieldY.toInt())
                        photoFocus.requestFocus()
                    }
                },
                secondary = true,
            )
        }

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
            label = "URL da foto HTTPS (opcional)",
            error = state.photoUrlError,
            keyboardType = KeyboardType.Uri,
            modifier = Modifier
                .focusRequester(photoFocus)
                .onGloballyPositioned { photoFieldY = it.positionInParent().y },
        )
        Spacer(modifier = Modifier.height(12.dp))
        FormTextField(
            value = state.instagram,
            onValueChange = viewModel::onInstagramChange,
            label = "Instagram (usuário, sem @)",
            error = state.instagramError,
            modifier = Modifier
                .focusRequester(instagramFocus)
                .onGloballyPositioned { instagramFieldY = it.positionInParent().y },
        )

        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle("Sobre você")
        Text(
            text = "Seu momento de vida e o que você busca por aqui.",
            color = SociableTheme.muted,
            fontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 4.dp),
        )

        Text(
            text = "Situação de vida",
            color = SociableTheme.ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 8.dp),
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LifeStage.entries.forEach { stage ->
                SociableChip(
                    text = stage.label,
                    selected = state.lifeStage == stage,
                    onClick = { viewModel.onLifeStageSelect(stage) },
                )
            }
        }

        Text(
            text = "O que você procura · ${state.lookingFor.size} de " +
                "${LookingForRules.MAX_CHOICES}",
            color = SociableTheme.ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 8.dp),
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LookingFor.entries.forEach { option ->
                val selected = option in state.lookingFor
                SociableChip(
                    text = option.label,
                    selected = selected,
                    onClick = {
                        if (selected || state.lookingFor.size < LookingForRules.MAX_CHOICES) {
                            viewModel.onLookingForToggle(option)
                        }
                    },
                )
            }
        }
        if (state.lookingFor.size == LookingForRules.MAX_CHOICES) {
            Text(
                text = "Limite de ${LookingForRules.MAX_CHOICES}. " +
                    "Toque em um escolhido para trocar.",
                color = SociableTheme.muted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Text(
            text = "Vibe social",
            color = SociableTheme.ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 8.dp),
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SocialVibe.entries.forEach { vibe ->
                SociableChip(
                    text = vibe.label,
                    selected = state.socialVibe == vibe,
                    onClick = { viewModel.onSocialVibeSelect(vibe) },
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle("Aparência")
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ThemeMode.entries.forEach { option ->
                SociableChip(
                    text = option.label,
                    selected = ThemeController.mode == option,
                    onClick = { ThemeController.setMode(option, context) },
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Meus interesses",
            color = SociableTheme.ink,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
        )
        Text(
            text = "Usamos isso para sugerir rolês com a sua cara.",
            color = SociableTheme.muted,
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
                color = SociableTheme.ink,
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
