package com.example.sociable.ui.messages

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.data.model.Community
import com.example.sociable.feature.community.CommunityListViewModel
import com.example.sociable.ui.common.BackLink
import com.example.sociable.ui.common.EmptyState
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

        // Minhas comunidades
        if (state.myCommunities.isNotEmpty()) {
            Text(
                text = "Minhas comunidades",
                color = SociableTheme.ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 12.dp),
            )
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
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Comunidades disponíveis
        Text(
            text = "Comunidades disponíveis",
            color = SociableTheme.ink,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 12.dp),
        )

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
                EmptyState(
                    title = "Nenhuma comunidade disponível",
                    subtitle = "Em breve aparecerão comunidades públicas aqui.",
                    modifier = Modifier.padding(top = 48.dp),
                )
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

        Spacer(modifier = Modifier.height(20.dp))
        SociableButton(
            text = "Criar comunidade",
            onClick = onCreateCommunity,
            secondary = true,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Composable
fun CommunityCard(
    community: Community,
    isJoined: Boolean,
    onJoin: () -> Unit,
    onClick: () -> Unit,
) {
    StickerBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        fill = if (isJoined) SociableTheme.accentSoft else SociableTheme.surface,
        radius = 18.dp,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = community.icon.orEmpty(),
                        fontSize = 24.sp,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = community.name,
                        color = SociableTheme.ink,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (!community.isPublic) {
                        Spacer(modifier = Modifier.width(8.dp))
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
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = "${community.memberCount} ${if (community.memberCount == 1) "membro" else "membros"}",
                    color = SociableTheme.muted,
                    fontSize = 12.sp,
                )
            }
            if (!isJoined) {
                SociableButton(
                    text = "Entrar",
                    onClick = onJoin,
                    modifier = Modifier.width(80.dp),
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