package com.example.sociable.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.core.time.ManausTime
import com.example.sociable.feature.events.EventDetailViewModel
import com.example.sociable.ui.common.ChipLabel
import com.example.sociable.ui.common.EmptyState
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.InfoRow
import com.example.sociable.ui.common.PrimaryButton
import com.example.sociable.ui.common.SecondaryButton
import com.example.sociable.ui.common.SociableTopBar
import com.example.sociable.ui.common.formatAverage
import com.example.sociable.ui.common.formatPrice

@Composable
fun EventDetailScreen(
    viewModel: EventDetailViewModel,
    onBack: () -> Unit,
    onChatClick: () -> Unit,
    onReviewsClick: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val event = state.event

    Scaffold(
        topBar = { SociableTopBar(title = "Detalhes do evento", onBack = onBack) },
    ) { padding ->
        when {
            state.isLoading && event == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            event == null -> {
                EmptyState(
                    title = "Evento não encontrado",
                    subtitle = "Ele pode ter sido removido pelo organizador.",
                    modifier = Modifier.padding(padding),
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ChipLabel(text = event.category.label)
                        Text(
                            text = if (event.isFree) {
                                "Grátis"
                            } else {
                                formatPrice(event.price ?: 0.0)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        text = if (state.rating.hasReviews) {
                            "★ ${formatAverage(state.rating.average)} · ${state.rating.count} avaliações"
                        } else {
                            "Sem avaliações ainda"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    InfoRow(
                        label = "Quando",
                        value = "${ManausTime.dateTime(event.startAt)} até ${ManausTime.time(event.endAt)}",
                    )
                    InfoRow(label = "Local", value = event.venueName)
                    InfoRow(label = "Endereço", value = event.address)
                    InfoRow(label = "Organizador", value = event.organizerName)
                    InfoRow(
                        label = "Confirmados",
                        value = buildString {
                            append("${state.rsvp.goingCount} pessoa(s)")
                            event.capacity?.let { append(" · $it vagas") }
                        },
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ErrorText(error = state.error)

                    if (state.rsvp.isFull && !state.rsvp.isGoing) {
                        Text(
                            text = "Este evento está lotado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    PrimaryButton(
                        text = if (state.rsvp.isGoing) "Cancelar presença" else "Eu vou!",
                        onClick = viewModel::onToggleRsvp,
                        isLoading = state.isTogglingRsvp,
                        enabled = state.rsvp.isGoing || !state.rsvp.isFull,
                    )

                    SecondaryButton(text = "Chat do evento", onClick = onChatClick)
                    SecondaryButton(text = "Avaliações e feedback", onClick = onReviewsClick)

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
