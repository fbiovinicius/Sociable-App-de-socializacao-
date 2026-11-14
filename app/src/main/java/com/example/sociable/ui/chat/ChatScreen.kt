package com.example.sociable.ui.chat

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
import com.example.sociable.feature.chat.ChatViewModel
import com.example.sociable.ui.common.BackLink
import com.example.sociable.ui.common.EmptyState
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.Pin
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.common.SociableTextField
import com.example.sociable.ui.common.StickerBox
import com.example.sociable.ui.common.message
import com.example.sociable.ui.theme.Coral
import com.example.sociable.ui.theme.Plum
import com.example.sociable.ui.theme.Rose
import com.example.sociable.ui.theme.SCream
import com.example.sociable.ui.theme.SInk

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val listState = rememberLazyListState()
    val inputError = state.inputError

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SCream)
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
            Column {
                Text(
                    text = state.eventTitle.ifBlank { "Chat do evento" },
                    color = SInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Text(
                    text = "${state.goingCount} confirmados",
                    color = Plum,
                    fontSize = 12.sp,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .background(SInk),
        )

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = SInk)
                }
            }

            state.messages.isEmpty() -> {
                EmptyState(
                    title = "Ninguém falou nada ainda",
                    subtitle = "Mande a primeira mensagem e combine os detalhes do rolê.",
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 32.dp),
                )
            }

            else -> {
                LazyColumn(
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
                                color = Plum,
                                fontSize = 11.sp,
                            )
                            StickerBox(
                                modifier = Modifier.fillMaxWidth(0.8f),
                                fill = if (isMine) Coral else Rose,
                                radius = 18.dp,
                                shadow = 0.dp,
                                contentPadding = PaddingValues(12.dp),
                            ) {
                                Text(
                                    text = message.text,
                                    color = if (isMine) SCream else SInk,
                                    fontSize = 14.sp,
                                )
                            }
                            Text(
                                text = ManausTime.time(message.sentAt),
                                color = Plum,
                                fontSize = 10.sp,
                            )
                        }
                    }
                }
            }
        }

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
        ErrorText(
            error = state.error,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }
}
