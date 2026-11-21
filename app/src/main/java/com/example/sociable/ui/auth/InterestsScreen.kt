package com.example.sociable.ui.auth

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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.data.model.Interest
import com.example.sociable.feature.profile.ProfileViewModel
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.ScreenTitle
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.common.SociableTextLink
import com.example.sociable.ui.theme.SociableTheme

private const val MAX_INTERESTS = 5

private data class InterestLabel(val interest: Interest, val label: String)

private val interestGroups: List<Pair<String, List<InterestLabel>>> = listOf(
    "Rolês" to listOf(
        InterestLabel(Interest.MUSIC, "🎵 Música"),
        InterestLabel(Interest.PARTY, "🎉 Festas"),
        InterestLabel(Interest.FOOD, "🍽️ Gastronomia"),
        InterestLabel(Interest.GAMES, "🎲 Jogos"),
        InterestLabel(Interest.CINEMA, "🎬 Cinema"),
        InterestLabel(Interest.DANCE, "💃 Dança"),
        InterestLabel(Interest.COFFEE, "☕ Café"),

    ),
    "Cultura" to listOf(
        InterestLabel(Interest.ART, "🎨 Arte e cultura"),
        InterestLabel(Interest.CRAFTS, "🧶 Artesanato"),
        InterestLabel(Interest.PHOTO, "📸 Fotografia"),
        InterestLabel(Interest.THEATER, "🎭 Teatro"),
        InterestLabel(Interest.READING, "📖 Leitura"),
        InterestLabel(Interest.ACADEMIC, "📚 Acadêmico"),
        InterestLabel(Interest.RELIGION, "🙏 Religioso"),
    ),
    "Ao ar livre" to listOf(
        InterestLabel(Interest.SPORTS, "⚽ Esporte"),
        InterestLabel(Interest.OUTDOORS, "🌿 Ao ar livre"),
    ),
    "Pessoas" to listOf(
        InterestLabel(Interest.TECH, "💻 Tecnologia"),
    ),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestsScreen(
    viewModel: ProfileViewModel,
    onDone: () -> Unit,
    onSkip: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SociableTheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
    ) {
        ScreenTitle(
            text = "Do seu jeito",
            sub = "Escolha até $MAX_INTERESTS · ${state.interests.size} de $MAX_INTERESTS",
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = SociableTheme.ink)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                interestGroups.forEach { (group, items) ->
                    Text(
                        text = group,
                        color = SociableTheme.ink,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items.forEach { item ->
                            val selected = item.interest in state.interests
                            SociableChip(
                                text = item.label,
                                selected = selected,
                                onClick = {
                                    if (selected || state.interests.size < MAX_INTERESTS) {
                                        viewModel.onInterestToggle(item.interest)
                                    }
                                },
                            )
                        }
                    }
                }
                if (state.interests.size == MAX_INTERESTS) {
                    Text(
                        text = "Você chegou ao limite de $MAX_INTERESTS. Toque em um para trocar.",
                        color = SociableTheme.muted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }

        ErrorText(error = state.error)
        Spacer(modifier = Modifier.height(12.dp))
        SociableButton(
            text = "Próximo",
            onClick = viewModel::onSaveClick,
            enabled = state.interests.isNotEmpty(),
            isLoading = state.isSaving,
        )
        SociableTextLink(
            text = "Agora não",
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
