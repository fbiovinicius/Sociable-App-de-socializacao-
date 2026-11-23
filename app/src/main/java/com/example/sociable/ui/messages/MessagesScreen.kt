package com.example.sociable.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.R
import com.example.sociable.core.time.ManausTime
import com.example.sociable.feature.messages.Conversation
import com.example.sociable.feature.messages.ConversationKind
import com.example.sociable.feature.messages.MessagesTab
import com.example.sociable.feature.messages.MessagesUiState
import com.example.sociable.feature.messages.MessagesViewModel
import com.example.sociable.ui.common.EmptyState
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.Pin
import com.example.sociable.ui.common.ScreenTitle
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.common.SociableTextField
import com.example.sociable.ui.common.StickerBox
import com.example.sociable.ui.common.message
import com.example.sociable.ui.theme.SociableTheme

/**
 * Aba Mensagens: chats dos eventos em que a pessoa vai (ou que organiza) e as
 * comunidades abertas, que qualquer pessoa pode criar e em que pode entrar.
 */
@Composable
fun MessagesScreen(
    viewModel: MessagesViewModel,
    onEventChatClick: (String) -> Unit,
    onCommunityClick: (String) -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SociableTheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ScreenTitle(text = "Mensagens", modifier = Modifier.weight(1f))
            SociableChip(
                text = "+ Comunidade",
                selected = true,
                onClick = viewModel::onCreateClick,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        SociableTextField(
            label = "Buscar",
            value = state.query,
            onChange = viewModel::onQueryChange,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MessagesTab.entries.forEach { tab ->
                SociableChip(
                    text = tab.label,
                    selected = state.tab == tab,
                    onClick = { viewModel.onTabChange(tab) },
                )
            }
        }
        ErrorText(error = state.error)
        Spacer(modifier = Modifier.height(10.dp))

        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = SociableTheme.ink)
            }

            state.conversations.isEmpty() -> {
                val (title, subtitle) = emptyTexts(state)
                EmptyState(
                    title = title,
                    subtitle = subtitle,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 32.dp),
                )
            }

            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items = state.conversations, key = { "${it.kind}-${it.id}" }) { conversation ->
                    ConversationRow(
                        conversation = conversation,
                        isJoining = state.joiningId == conversation.id,
                        onClick = {
                            when (conversation.kind) {
                                ConversationKind.EVENT -> onEventChatClick(conversation.id)
                                ConversationKind.COMMUNITY -> onCommunityClick(conversation.id)
                            }
                        },
                        onJoin = { viewModel.onJoinCommunity(conversation.id) },
                    )
                }
            }
        }
    }

    if (state.showCreateDialog) {
        CreateCommunityDialog(state = state, viewModel = viewModel)
    }
}

private fun emptyTexts(state: MessagesUiState): Pair<String, String> = when {
    state.query.isNotBlank() -> "Nada encontrado" to "Tente outro nome."
    state.tab == MessagesTab.EVENTS ->
        "Nenhum chat de evento" to "Confirme presença em um evento para conversar com quem vai."
    state.tab == MessagesTab.COMMUNITIES ->
        "Ainda sem comunidades" to "Toque em \"+ Comunidade\" para criar a primeira."
    else ->
        "Nenhuma conversa ainda" to "Confirme presença em um evento ou entre em uma comunidade."
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    isJoining: Boolean,
    onClick: () -> Unit,
    onJoin: () -> Unit,
) {
    StickerBox(
        modifier = Modifier.fillMaxWidth(),
        fill = SociableTheme.surface,
        radius = 18.dp,
        shadow = 3.dp,
        contentPadding = PaddingValues(12.dp),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Pin(
                id = if (conversation.kind == ConversationKind.EVENT) {
                    R.drawable.ic_pin_party
                } else {
                    R.drawable.ic_pin_people
                },
                size = 36.dp,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.title,
                    color = SociableTheme.ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = conversation.preview,
                    color = SociableTheme.muted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle(conversation),
                    color = SociableTheme.muted,
                    fontSize = 11.sp,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (conversation.kind == ConversationKind.COMMUNITY && !conversation.isMember) {
                SociableChip(
                    text = if (isJoining) "…" else "Entrar",
                    selected = true,
                    onClick = { if (!isJoining) onJoin() },
                )
            } else {
                conversation.lastActivity?.let { time ->
                    Text(
                        text = ManausTime.time(time),
                        color = SociableTheme.muted,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}

private fun subtitle(conversation: Conversation): String {
    val people = if (conversation.kind == ConversationKind.EVENT) {
        "${conversation.memberCount} confirmados"
    } else {
        "${conversation.memberCount} membros"
    }
    return if (conversation.kind == ConversationKind.EVENT) "Evento · $people" else "Comunidade · $people"
}

@Composable
private fun CreateCommunityDialog(state: MessagesUiState, viewModel: MessagesViewModel) {
    Dialog(onDismissRequest = { if (!state.isCreating) viewModel.onCreateDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SociableTheme.surface,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Nova comunidade",
                    color = SociableTheme.ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "Um grupo aberto: qualquer pessoa do Sociable pode entrar e conversar.",
                    color = SociableTheme.muted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                )
                SociableTextField(
                    label = "Nome da comunidade",
                    value = state.newName,
                    onChange = viewModel::onNewNameChange,
                    isError = state.newNameError != null,
                    supportingText = state.newNameError?.message(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                Spacer(modifier = Modifier.height(8.dp))
                SociableTextField(
                    label = "Descrição (opcional)",
                    value = state.newDescription,
                    onChange = viewModel::onNewDescriptionChange,
                    isError = state.newDescriptionError != null,
                    supportingText = state.newDescriptionError?.message(),
                    singleLine = false,
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                ErrorText(error = state.createError)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = viewModel::onCreateDismiss, enabled = !state.isCreating) {
                        Text("Cancelar")
                    }
                    TextButton(onClick = viewModel::onCreateSubmit, enabled = !state.isCreating) {
                        if (state.isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(18.dp),
                                color = SociableTheme.ink,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Criar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
