package com.example.sociable.ui.events

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.core.time.ManausTime
import com.example.sociable.feature.events.EventDetailViewModel
import com.example.sociable.ui.common.BackLink
import com.example.sociable.ui.common.EmptyState
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.Pin
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.common.StickerBox
import com.example.sociable.ui.common.formatAverage
import com.example.sociable.ui.common.formatEventDay
import com.example.sociable.ui.common.formatPriceText
import com.example.sociable.ui.common.formatSpots
import com.example.sociable.ui.common.pinRes
import com.example.sociable.ui.theme.OrangeSoft
import com.example.sociable.ui.theme.Plum
import com.example.sociable.ui.theme.Rose
import com.example.sociable.ui.theme.SCream
import com.example.sociable.ui.theme.SInk

@Composable
fun EventDetailScreen(
    viewModel: EventDetailViewModel,
    onBack: () -> Unit,
    onChatClick: () -> Unit,
    onReviewsClick: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val event = state.event

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SCream)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        when {
            state.isLoading && event == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = SInk)
                }
            }

            event == null -> {
                EmptyState(
                    title = "Evento não encontrado",
                    subtitle = "Ele pode ter sido removido pelo organizador.",
                    modifier = Modifier.padding(top = 48.dp),
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 24.dp, end = 28.dp, bottom = 24.dp),
                ) {
                    BackLink(onBack)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Pin(pinRes(event.category, event.isSecret), size = 56.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SociableChip(
                                text = if (event.isSecret) "🔒 Segredinho" else event.category.label,
                                selected = true,
                                onClick = {},
                            )
                            Text(
                                text = "por ${event.organizerName}",
                                color = Plum,
                                fontSize = 12.sp,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = event.title,
                        color = SInk,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )

                    Text(
                        text = if (state.rating.hasReviews) {
                            "★ ${formatAverage(state.rating.average)} · ${state.rating.count} avaliações"
                        } else {
                            "Sem avaliações ainda"
                        },
                        color = Plum,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
                    )

                    StickerBox(
                        modifier = Modifier.fillMaxWidth(),
                        fill = Rose,
                        radius = 20.dp,
                        contentPadding = PaddingValues(16.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "📅 ${formatEventDay(event.startAt)} · ${ManausTime.time(event.startAt)} até ${ManausTime.time(event.endAt)}",
                                color = SInk,
                                fontSize = 14.sp,
                            )
                            Text(
                                text = if (event.isSecret && !state.rsvp.isGoing) {
                                    "📍 Local revelado 2 h antes, só para convidados"
                                } else {
                                    "📍 ${event.venueName} · ${event.address}"
                                },
                                color = SInk,
                                fontSize = 14.sp,
                            )
                            Text(
                                text = "💰 ${formatPriceText(event.isFree, event.price)}",
                                color = SInk,
                                fontSize = 14.sp,
                            )
                            Text(
                                text = "👥 ${formatSpots(state.rsvp.goingCount, event.capacity)}",
                                color = SInk,
                                fontSize = 14.sp,
                            )
                            if (!event.isSecret || state.rsvp.isGoing) {
                                Text(
                                    text = "🧭 Lat ${event.latitude} · Lon ${event.longitude}",
                                    color = Plum,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Sobre o rolê",
                        color = SInk,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = event.description,
                        color = SInk,
                        fontSize = 14.sp,
                    )

                    if (event.isSecret) {
                        Spacer(modifier = Modifier.height(12.dp))
                        StickerBox(
                            modifier = Modifier.fillMaxWidth(),
                            fill = OrangeSoft,
                            radius = 14.dp,
                            contentPadding = PaddingValues(14.dp),
                        ) {
                            Text(
                                text = "🔒 O endereço completo é revelado 2 h antes, só para quem tem convite.",
                                color = SInk,
                                fontSize = 13.sp,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    ErrorText(error = state.error)

                    if (state.rsvp.isFull && !state.rsvp.isGoing) {
                        Text(
                            text = "Este evento está lotado.",
                            color = SInk,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }

                    SociableButton(
                        text = when {
                            event.isSecret && state.rsvp.isGoing ->
                                "Convite confirmado ✓ (toque para cancelar)"
                            event.isSecret -> "Pedir convite"
                            state.rsvp.isGoing -> "Você vai! ✓ (toque para cancelar)"
                            else -> "Eu vou!"
                        },
                        onClick = viewModel::onToggleRsvp,
                        secondary = state.rsvp.isGoing,
                        isLoading = state.isTogglingRsvp,
                        enabled = state.rsvp.isGoing || !state.rsvp.isFull,
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    SociableButton(
                        text = "Chat do evento",
                        onClick = onChatClick,
                        secondary = true,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SociableButton(
                        text = "Avaliações e feedback",
                        onClick = onReviewsClick,
                        secondary = true,
                    )
                }
            }
        }
    }
}
