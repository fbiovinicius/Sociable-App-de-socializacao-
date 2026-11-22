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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.feature.messages.ConversationEvent
import com.example.sociable.feature.messages.MessagesViewModel
import com.example.sociable.ui.common.BackLink
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.formatEventDay
import com.example.sociable.ui.common.formatSpots
import com.example.sociable.ui.theme.SociableTheme

@Composable
fun ChatInfoScreen(
    viewModel: MessagesViewModel,
    event: ConversationEvent,
    onEnterChat: () -> Unit,
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
            text = "Informações do chat",
            color = SociableTheme.ink,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(SociableTheme.surface, androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = event.title,
                    color = SociableTheme.ink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "${event.categoryLabel} · ${formatEventDay(event.startAt)}",
                    color = SociableTheme.muted,
                    fontSize = 13.sp,
                )
                Text(
                    text = "📍 ${event.venueName} · ${event.address}",
                    color = SociableTheme.ink,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "👥 ${formatSpots(event.goingCount, null)}",
                    color = SociableTheme.ink,
                    fontSize = 14.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Quem está participando",
            color = SociableTheme.ink,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Placeholder para avatares dos participantes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (i in 1..4) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "?", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (event.goingCount > 4) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SociableTheme.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+${event.goingCount - 4}",
                        color = SociableTheme.onAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SociableButton(
            text = "Entrar no chat",
            onClick = onEnterChat,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        state.error?.let { error ->
            Text(
                text = error.toString(),
                color = SociableTheme.error,
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp),
            )
        }
    }
}