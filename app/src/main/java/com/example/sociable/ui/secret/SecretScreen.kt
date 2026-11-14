package com.example.sociable.ui.secret

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.core.time.ManausTime
import com.example.sociable.feature.events.EventListViewModel
import com.example.sociable.ui.common.EmptyState
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.ScreenTitle
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.common.StickerBox
import com.example.sociable.ui.common.formatEventDay
import com.example.sociable.ui.common.formatPriceText
import com.example.sociable.ui.common.formatSpots
import com.example.sociable.ui.theme.OrangeSoft
import com.example.sociable.ui.theme.Plum
import com.example.sociable.ui.theme.SCream
import com.example.sociable.ui.theme.SInk

@Composable
fun SecretScreen(
    viewModel: EventListViewModel,
    onEventClick: (String) -> Unit,
    onCreateClick: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        viewModel.onOnlySecretChange(true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SCream)
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 24.dp, top = 16.dp)) {
            ScreenTitle("Segredinhos ✦", sub = "Rolês que não são divulgados por aí")
        }

        ErrorText(
            error = state.error,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        val loading = state.isLoading || !state.filter.onlySecret
        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = SInk)
                }
            }

            state.events.isEmpty() -> {
                Column(modifier = Modifier.weight(1f)) {
                    EmptyState(
                        title = "Nenhum segredinho por enquanto",
                        subtitle = "Crie um rolê com local revelado só aos convidados.",
                        modifier = Modifier.padding(top = 32.dp),
                    )
                }
                Box(modifier = Modifier.padding(24.dp)) {
                    SociableButton(
                        text = "+ Criar segredinho",
                        onClick = onCreateClick,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 24.dp, top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(items = state.events, key = { it.id }) { event ->
                        StickerBox(
                            modifier = Modifier.fillMaxWidth(),
                            fill = OrangeSoft,
                            radius = 22.dp,
                            contentPadding = PaddingValues(16.dp),
                            onClick = { onEventClick(event.id) },
                        ) {
                            Column {
                                Text(
                                    text = "🔒 Secreto",
                                    color = SInk,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = event.title,
                                    color = SInk,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                                Text(
                                    text = "${formatEventDay(event.startAt)} · ${ManausTime.time(event.startAt)}",
                                    color = SInk,
                                    fontSize = 13.sp,
                                )
                                Text(
                                    text = "📍 Local revelado 2 h antes para quem confirmar",
                                    color = Plum,
                                    fontSize = 12.sp,
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "👥 ${formatSpots(event.goingCount, event.capacity)} · ${formatPriceText(event.isFree, event.price)}",
                                        color = SInk,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f),
                                    )
                                    SociableChip(
                                        text = "Ver detalhes",
                                        selected = false,
                                        onClick = { onEventClick(event.id) },
                                    )
                                }
                            }
                        }
                    }
                }
                Box(modifier = Modifier.padding(24.dp)) {
                    SociableButton(
                        text = "+ Criar evento",
                        onClick = onCreateClick,
                    )
                }
            }
        }
    }
}
