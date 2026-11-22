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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.example.sociable.feature.messages.ConversationEvent
import com.example.sociable.feature.messages.MessagesViewModel
import com.example.sociable.ui.common.BackLink
import com.example.sociable.ui.common.EmptyState
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.StickerBox
import com.example.sociable.ui.common.formatEventDay
import com.example.sociable.ui.theme.SociableTheme

@Composable
fun MessagesScreen(
    viewModel: MessagesViewModel,
    onConversationClick: (ConversationEvent) -> Unit,
    onCommunityClick: () -> Unit,
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
            text = "Mensagens",
            color = SociableTheme.ink,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Abas simples: Conversas | Comunidades
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Conversas",
                    color = SociableTheme.accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Comunidades",
                    color = SociableTheme.muted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = SociableTheme.ink)
                }
            }

            state.conversations.isEmpty() -> {
                EmptyState(
                    title = "Nenhuma conversa",
                    subtitle = "Confirme presença em um evento para participar do chat.",
                    modifier = Modifier.padding(top = 48.dp),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp),
                ) {
                    items(
                        items = state.conversations,
                        key = { it.eventId },
                    ) { conversation ->
                        ConversationRow(
                            event = conversation,
                            onClick = { onConversationClick(conversation) },
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
        )
    }
}

@Composable
fun ConversationRow(
    event: ConversationEvent,
    onClick: () -> Unit,
) {
    StickerBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        fill = SociableTheme.surface,
        radius = 18.dp,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    color = SociableTheme.ink,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    text = "${event.categoryLabel} · ${formatEventDay(event.startAt)}",
                    color = SociableTheme.muted,
                    fontSize = 12.sp,
                )
            }
            Column {
                Text(
                    text = "${event.goingCount} confirmados",
                    color = SociableTheme.ink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = event.address.take(30).ifBlank { event.venueName },
                    color = SociableTheme.muted,
                    fontSize = 12.sp,
                    maxLines = 1,
                )
            }
        }
    }
}