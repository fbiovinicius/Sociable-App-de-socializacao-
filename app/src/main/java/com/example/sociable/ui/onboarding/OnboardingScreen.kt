package com.example.sociable.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.data.model.LifeStage
import com.example.sociable.data.model.LookingFor
import com.example.sociable.data.model.LookingForRules
import com.example.sociable.data.model.SocialVibe
import com.example.sociable.feature.onboarding.OnboardingUiState
import com.example.sociable.feature.onboarding.OnboardingViewModel
import com.example.sociable.ui.common.BackLink
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.ScreenTitle
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.common.SociableTextLink
import com.example.sociable.ui.theme.SociableTheme

/**
 * Wizard de 3 passos exibido uma única vez, logo depois do cadastro:
 * situação de vida, o que procura e vibe social. Tudo é opcional e pode ser
 * editado depois no Perfil.
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(state.isDone) {
        if (state.isDone) onFinished()
    }
    BackHandler(enabled = state.step > 0) {
        viewModel.onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SociableTheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        if (state.step > 0) {
            BackLink(onBack = viewModel::onBack)
        } else {
            // Mantém o conteúdo alinhado com as etapas que têm "Voltar".
            Spacer(modifier = Modifier.height(48.dp))
        }

        StepProgress(
            current = state.step,
            total = OnboardingViewModel.STEP_COUNT,
        )
        Text(
            text = "Passo ${state.step + 1} de ${OnboardingViewModel.STEP_COUNT}",
            color = SociableTheme.muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
        ) {
            when (state.step) {
                0 -> LifeStageStep(state, viewModel)
                1 -> LookingForStep(state, viewModel)
                else -> SocialVibeStep(state, viewModel)
            }
        }

        ErrorText(error = state.error)
        Spacer(modifier = Modifier.height(8.dp))
        SociableButton(
            text = if (state.step == OnboardingViewModel.STEP_COUNT - 1) {
                "Concluir"
            } else {
                "Continuar"
            },
            onClick = viewModel::onNext,
            isLoading = state.isSaving,
        )
        SociableTextLink(
            text = "Pular esta etapa",
            onClick = viewModel::onSkipStep,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StepProgress(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (index <= current) SociableTheme.accent else SociableTheme.chip,
                    ),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LifeStageStep(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
) {
    ScreenTitle(
        text = "Qual é a sua situação?",
        sub = "Como é o seu momento de vida hoje. Dá para mudar depois no perfil.",
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LifeStage.entries.forEach { stage ->
            SociableChip(
                text = stage.label,
                selected = state.lifeStage == stage,
                onClick = { viewModel.onLifeStageSelect(stage) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LookingForStep(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
) {
    ScreenTitle(
        text = "O que você procura?",
        sub = "Escolha até ${LookingForRules.MAX_CHOICES} · " +
            "${state.lookingFor.size} de ${LookingForRules.MAX_CHOICES}",
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SocialVibeStep(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
) {
    ScreenTitle(
        text = "Qual é a sua vibe?",
        sub = "Como você curte os rolês. Dá para mudar depois no perfil.",
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SocialVibe.entries.forEach { vibe ->
            SociableChip(
                text = vibe.label,
                selected = state.socialVibe == vibe,
                onClick = { viewModel.onSocialVibeSelect(vibe) },
            )
        }
    }
}
