package com.example.sociable.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.data.model.Community
import com.example.sociable.feature.community.CommunityListViewModel
import com.example.sociable.ui.common.BackLink
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.common.StickerBox
import com.example.sociable.ui.theme.SociableTheme

@Composable
fun CommunityListScreen(
    viewModel: CommunityListViewModel,
    onCommunityClick: (Community) -> Unit,
    onJoinCommunity: (String) -> Unit,
    onCreateCommunity: () -> Unit,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SociableTheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        BackLink(onBack)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Comunidades",
            color = SociableTheme.ink,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Filtros: Minhas / Disponíveis
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CommunityFilterChip(
                text = "Minhas",
                selected = true,
                onClick = { /* TODO */ },
            )
            CommunityFilterChip(
                text = "Disponíveis",
                selected = false,
                onClick = { /* TODO */ },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Minhas comunidades
        if (state.myCommunities.isNotEmpty()) {
            SectionTitle(text = "Minhas comunidades")
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp),
            ) {
                items(
                    items = state.myCommunities,
                    key = { it.id },
                ) { community ->
                    CommunityCard(
                        community = community,
                        isJoined = true,
                        onJoin = { onJoinCommunity(community.id) },
                        onClick = { onCommunityClick(community) },
                    )
                }
            }
        } else {
            // Placeholder quando não tem comunidades
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                // EmptyState inline
                Column(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .padding(start = 24.dp, top = 48.dp, end = 24.dp, bottom = 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Nenhuma comunidade ainda",
                        color = SociableTheme.ink,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Entre em uma comunidade ou crie a sua.",
                        color = SociableTheme.muted,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Comunidades disponíveis
        SectionTitle(text = "Disponíveis para entrar")

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = SociableTheme.ink)
                }
            }

            state.availableCommunities.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // EmptyState inline
                    Column(
                        modifier = androidx.compose.ui.Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                            .padding(start = 24.dp, top = 48.dp, end = 24.dp, bottom = 0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Nenhuma comunidade disponível",
                            color = SociableTheme.ink,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Em breve aparecerão comunidades públicas aqui.",
                            color = SociableTheme.muted,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp),
                ) {
                    items(
                        items = state.availableCommunities,
                        key = { it.id },
                    ) { community ->
                        CommunityCard(
                            community = community,
                            isJoined = state.myCommunities.any { it.id == community.id },
                            onJoin = { onJoinCommunity(community.id) },
                            onClick = { onCommunityClick(community) },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        SociableButton(
            text = "Criar comunidade",
            onClick = onCreateCommunity,
            secondary = true,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Composable
fun CommunityFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clickable(onClick = onClick)
            .background(
                if (selected) SociableTheme.accent else SociableTheme.chip,
                androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (selected) SociableTheme.onAccent else SociableTheme.ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = SociableTheme.ink,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = androidx.compose.ui.Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
    )
}

@Composable
fun CommunityCard(
    community: Community,
    isJoined: Boolean,
    onJoin: () -> Unit,
    onClick: () -> Unit,
) {
    StickerBox(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        fill = if (isJoined) SociableTheme.accentSoft else SociableTheme.surface,
        radius = 16.dp,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Ícone da comunidade - usa cor padrão
            val iconColor = SociableTheme.chip
            Box(
                modifier = androidx.compose.ui.Modifier.size(44.dp)
                    .background(
                        iconColor,
                        androidx.compose.foundation.shape.CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = community.icon.orEmpty(),
                    fontSize = 20.sp,
                )
            }
            Spacer(modifier = androidx.compose.ui.Modifier.width(12.dp))

            Column(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = community.name,
                        color = SociableTheme.ink,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (!community.isPublic) {
                        Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                        SociableChip(
                            text = "Privada",
                            selected = true,
                            onClick = {},
                        )
                    }
                }
                Text(
                    text = community.description,
                    color = SociableTheme.muted,
                    fontSize = 13.sp,
                    modifier = androidx.compose.ui.Modifier.padding(top = 4.dp),
                )
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${community.memberCount} ${if (community.memberCount == 1) "membro" else "membros"}",
                        color = SociableTheme.muted,
                        fontSize = 12.sp,
                    )
                }
            }
            if (!isJoined) {
                SociableButton(
                    text = "Entrar",
                    onClick = onJoin,
                    modifier = androidx.compose.ui.Modifier.width(80.dp),
                )
            } else {
                SociableChip(
                    text = "Participando",
                    selected = true,
                    onClick = {},
                )
            }
        }
    }
}