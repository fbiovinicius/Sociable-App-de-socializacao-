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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.core.time.ManausTime
import com.example.sociable.feature.events.EventDetailViewModel
import com.example.sociable.ui.common.BackLink
import com.example.sociable.ui.common.formatDistance
import com.example.sociable.ui.map.rememberUserLocation
import com.example.sociable.core.location.distanceKm
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import com.example.sociable.ui.theme.SociableTheme
import com.example.sociable.ui.common.AddImageBox

@Composable
fun EventDetailScreen(
    viewModel: EventDetailViewModel,
    onBack: () -> Unit,
    onChatClick: () -> Unit,
    onReviewsClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val event = state.event
    val isOrganizer = event != null && event.organizerId == viewModel.currentUserId
    var confirmingCancel by remember { mutableStateOf(false) }
    val userLocation by rememberUserLocation()

    // Lembrete local (bloco F1): no Android 13+ a notificação depende de
    // permissão em runtime; pedimos no momento em que a pessoa confirma presença.
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }
    fun ensureNotificationPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SociableTheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        when {
            state.isLoading && event == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = SociableTheme.ink)
                }
            }

            event == null && state.error != null -> {
                Column(modifier = Modifier.padding(24.dp)) {
                    BackLink(onBack)
                    Spacer(modifier = Modifier.height(24.dp))
                    ErrorText(error = state.error)
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

                    // Capa do evento. Por enquanto só visual: trocar a imagem depende
                    // de suporte na EventDetailViewModel (fora do escopo de UI).
                    AddImageBox(
                        title = "Adicionar imagem do evento",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(168.dp),
                        )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Pin(pinRes(event.category, event.isSecret), size = 56.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SociableChip(
                                text = if (event.isSecret) "🔒 Chat da Cidade" else event.category.label,
                                selected = true,
                                onClick = {},
                            )
                            Text(
                                text = "por ${event.organizerName}",
                                color = SociableTheme.muted,
                                fontSize = 12.sp,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = event.title,
                        color = SociableTheme.ink,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )

                    Text(
                        text = if (state.rating.hasReviews) {
                            "★ ${formatAverage(state.rating.average)} · ${state.rating.count} avaliações"
                        } else {
                            "Sem avaliações ainda"
                        },
                        color = SociableTheme.muted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
                    )

                    StickerBox(
                        modifier = Modifier.fillMaxWidth(),
                        fill = SociableTheme.chip,
                        radius = 20.dp,
                        contentPadding = PaddingValues(16.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "📅 ${formatEventDay(event.startAt)} · ${ManausTime.time(event.startAt)} até ${ManausTime.time(event.endAt)}",
                                color = SociableTheme.ink,
                                fontSize = 14.sp,
                            )
                            Text(
                                text = if (event.isSecret && !state.rsvp.isGoing) {
                                    "📍 Local revelado para quem confirmar presença"
                                } else {
                                    "📍 ${event.venueName} · ${event.address}"
                                },
                                color = SociableTheme.ink,
                                fontSize = 14.sp,
                            )
                            Text(
                                text = "💰 ${formatPriceText(event.isFree, event.price)}",
                                color = SociableTheme.ink,
                                fontSize = 14.sp,
                            )
                            Text(
                                text = "👥 ${formatSpots(state.rsvp.goingCount, event.capacity)}",
                                color = SociableTheme.ink,
                                fontSize = 14.sp,
                            )
                            // Em segredinho, só quem já vê o local real (confirmou ou organiza) vê a distância.
                            val here = userLocation
                            if (here != null && (!event.isSecret || state.rsvp.isGoing || isOrganizer)) {
                                val km = distanceKm(here.latitude, here.longitude, event.latitude, event.longitude)
                                Text(
                                    text = "🧭 A ${formatDistance(km)} de você",
                                    color = SociableTheme.ink,
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Sobre o rolê",
                        color = SociableTheme.ink,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = event.description,
                        color = SociableTheme.ink,
                        fontSize = 14.sp,
                    )

                    if (event.isSecret) {
                        Spacer(modifier = Modifier.height(12.dp))
                        StickerBox(
                            modifier = Modifier.fillMaxWidth(),
                            fill = SociableTheme.accentSoft,
                            radius = 14.dp,
                            contentPadding = PaddingValues(14.dp),
                        ) {
                            Text(
                                text = "🔒 O endereço completo é revelado para quem confirmar presença.",
                                color = SociableTheme.ink,
                                fontSize = 13.sp,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    ErrorText(error = state.error)

                    if (state.rsvp.isFull && !state.rsvp.isGoing) {
                        Text(
                            text = "Este evento está lotado.",
                            color = SociableTheme.ink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }

                    if (state.isCancelled) {
                        StickerBox(
                            modifier = Modifier.fillMaxWidth(),
                            fill = SociableTheme.accentSoft,
                            radius = 14.dp,
                            contentPadding = PaddingValues(14.dp),
                        ) {
                            Text(
                                text = "🚫 Este evento foi cancelado pelo organizador.",
                                color = SociableTheme.ink,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (!state.isCancelled) SociableButton(
                        text = when {
                            event.isSecret && state.rsvp.isGoing ->
                                "Convite confirmado ✓ (toque para cancelar)"
                            event.isSecret -> "Pedir convite"
                            state.rsvp.isGoing -> "Você vai! ✓ (toque para cancelar)"
                            else -> "Eu vou!"
                        },
                        onClick = {
                            ensureNotificationPermission()
                            viewModel.onToggleRsvp()
                        },
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

                    if (isOrganizer && !state.isCancelled) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Você organiza este evento",
                            color = SociableTheme.ink,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SociableButton(
                            text = "Editar evento",
                            onClick = onEditClick,
                            secondary = true,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (!confirmingCancel) {
                            SociableButton(
                                text = "Cancelar evento",
                                onClick = { confirmingCancel = true },
                                secondary = true,
                            )
                        } else {
                            StickerBox(
                                modifier = Modifier.fillMaxWidth(),
                                fill = SociableTheme.accentSoft,
                                radius = 14.dp,
                                contentPadding = PaddingValues(14.dp),
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "Cancelar o evento? Ele some do feed e do mapa e as pessoas confirmadas vão ver o aviso de cancelamento. Isso não dá para desfazer pelo app.",
                                        color = SociableTheme.ink,
                                        fontSize = 13.sp,
                                    )
                                    SociableButton(
                                        text = "Sim, cancelar evento",
                                        onClick = {
                                            confirmingCancel = false
                                            viewModel.onCancelEvent()
                                        },
                                        isLoading = state.isCancelling,
                                    )
                                    SociableButton(
                                        text = "Voltar",
                                        onClick = { confirmingCancel = false },
                                        secondary = true,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
