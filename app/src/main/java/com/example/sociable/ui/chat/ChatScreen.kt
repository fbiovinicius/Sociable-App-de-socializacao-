package com.example.sociable.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.core.time.ManausTime
import com.example.sociable.feature.chat.ChatViewModel
import com.example.sociable.ui.common.EmptyState
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.SociableTopBar
import com.example.sociable.ui.common.message

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

    Scaffold(
        topBar = { SociableTopBar(title = "Chat do evento", onBack = onBack) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .imePadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                ErrorText(error = state.error)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = state.input,
                        onValueChange = viewModel::onInputChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Mensagem") },
                        singleLine = true,
                        isError = inputError != null,
                        supportingText = if (inputError != null) {
                            { Text(text = inputError.message()) }
                        } else {
                            null
                        },
                    )
                    TextButton(
                        onClick = viewModel::onSendClick,
                        enabled = !state.isSending,
                    ) {
                        Text(text = "Enviar")
                    }
                }
            }
        },
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.messages.isEmpty() -> {
                EmptyState(
                    title = "Ninguém falou nada ainda",
                    subtitle = "Mande a primeira mensagem e combine os detalhes do rolê.",
                    modifier = Modifier.padding(padding),
                )
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = state.messages, key = { it.id }) { message ->
                        val isMine = message.authorId == state.currentUserId
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
                        ) {
                            Text(
                                text = if (isMine) "Você" else message.authorName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isMine) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                Text(text = message.text)
                            }
                            Text(
                                text = ManausTime.time(message.sentAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
