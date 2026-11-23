package com.example.sociable.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.R
import com.example.sociable.core.time.ManausTime
import com.example.sociable.feature.messages.CommunityChatViewModel
import com.example.sociable.ui.common.BackLink
import com.example.sociable.ui.common.EmptyState
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.Pin
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.common.SociableTextField
import com.example.sociable.ui.common.StickerBox
import com.example.sociable.ui.common.message
import com.example.sociable.ui.theme.SociableTheme

/** Chat de uma comunidade. Quem ainda não entrou vê só o convite para entrar. */
@Composable
fun CommunityChatScreen(
    viewModel: CommunityChatViewModel,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val listState = rememberLazyListState()
    val inputError = state.inputError

    LaunchedEffect(state.hasLeft) {
        if (state.hasLeft) onBack()
    }
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SociableTheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        BackLink(onBack)

        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Pin(R.drawable.ic_pin_people, size = 40.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.name.ifBlank { "Comunidade" },
                    color = SociableTheme.ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Text(
                    text = "${state.memberCount} membros",
                    color = SociableTheme.muted,
                    fontSize = 12.sp,
                )
            }
            if (state.isMember && !state.isOwner) {
                SociableChip(text = "Sair", selected = false, onClick = viewModel::onLeaveClick)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .background(SociableTheme.ink),
        )

        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = SociableTheme.ink)
            }

            !state.isMember -> Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                EmptyState(
                    title = "Entre para conversar",
                    subtitle = "As mensagens ficam visíveis só para quem faz parte da comunidade.",
                    modifier = Modifier.padding(top = 32.dp),
                )
                SociableButton(text = "Entrar na comunidade", onClick = viewModel::onJoinClick)
            }

            state.messages.isEmpty() -> EmptyState(
                title = "Ninguém falou nada ainda",
                subtitle = "Mande a primeira mensagem e comece a conversa.",
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 32.dp),
            )

            else -> LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items = state.messages, key = { it.id }) { message ->
                    val isMine = message.authorId == state.currentUserId
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
                    ) {
                        Text(
                            text = if (isMine) "Você" else message.authorName,
                            color = SociableTheme.muted,
                            fontSize = 11.sp,
                        )
                        StickerBox(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            fill = if (isMine) SociableTheme.accent else SociableTheme.chip,
                            radius = 18.dp,
                            shadow = 0.dp,
                            contentPadding = PaddingValues(12.dp),
                        ) {
                            Text(
                                text = message.text,
                                color = if (isMine) SociableTheme.onAccent else SociableTheme.ink,
                                fontSize = 14.sp,
                            )
                        }
                        Text(
                            text = ManausTime.time(message.sentAt),
                            color = SociableTheme.muted,
                            fontSize = 10.sp,
                        )
                    }
                }
            }
        }

        if (state.isMember) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SociableTextField(
                    label = "Mensagem",
                    value = state.input,
                    onChange = viewModel::onInputChange,
                    modifier = Modifier.weight(1f),
                    isError = inputError != null,
                    supportingText = inputError?.message(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send,
                    ),
                    keyboardActions = KeyboardActions(onSend = { viewModel.onSendClick() }),
                )
                SociableChip(
                    text = if (state.isSending) "…" else "Enviar",
                    selected = true,
                    onClick = { if (!state.isSending) viewModel.onSendClick() },
                )
            }
        }
        ErrorText(
            error = state.error,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }
}
