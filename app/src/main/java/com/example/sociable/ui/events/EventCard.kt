package com.example.sociable.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sociable.core.time.ManausTime
import com.example.sociable.data.model.Event
import com.example.sociable.ui.common.Pin
import com.example.sociable.ui.common.StickerBox
import com.example.sociable.ui.common.formatEventDay
import com.example.sociable.ui.common.formatPriceText
import com.example.sociable.ui.common.formatSpots
import com.example.sociable.ui.common.pinRes
import com.example.sociable.ui.theme.SociableTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import java.time.Instant

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StickerBox(
        modifier = modifier.fillMaxWidth(),
        fill = if (event.isSecret) SociableTheme.accentSoft else SociableTheme.background,
        radius = 22.dp,
        contentPadding = PaddingValues(16.dp),
        onClick = onClick,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pin(pinRes(event.category, event.isSecret), size = 38.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (event.isSecret) "🔒 Segredinho" else event.category.label,
                        color = SociableTheme.muted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${formatEventDay(event.startAt)} · ${ManausTime.time(event.startAt)}",
                        color = SociableTheme.muted,
                        fontSize = 12.sp,
                    )
                }
                Text(
                    text = formatPriceText(event.isFree, event.price),
                    color = SociableTheme.ink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            val now = remember { Instant.now() }
            val isNow = event.isHappeningAt(now)
            val capacity = event.capacity
            val isFull = capacity != null && event.goingCount >= capacity
            if (isNow || isFull) {
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (isNow) StatusBadge(text = "Agora", filled = true)
                    if (isFull) StatusBadge(text = "Lotado", filled = false)
                }
            }

            Text(
                text = event.title,
                color = SociableTheme.ink,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                text = if (event.isSecret) {
                    "📍 Local revelado para quem confirmar presença"
                } else {
                    "📍 ${event.venueName} · ${event.address}"
                },
                color = SociableTheme.ink,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )

            if (capacity != null && capacity > 0) {
                val progress = (event.goingCount.toFloat() / capacity).coerceIn(0f, 1f)
                val barShape = RoundedCornerShape(4.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(8.dp)
                        .clip(barShape)
                        .background(SociableTheme.chip)
                        .border(1.5.dp, SociableTheme.ink, barShape),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(SociableTheme.accent),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "👥 ${formatSpots(event.goingCount, event.capacity)}",
                    color = SociableTheme.ink,
                    fontSize = 13.sp,
                )
                Text(
                    text = "Ver detalhes →",
                    color = SociableTheme.ink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, filled: Boolean) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .background(if (filled) SociableTheme.accent else SociableTheme.accentSoft, shape)
            .border(1.5.dp, SociableTheme.ink, shape)
            .padding(horizontal = 10.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            color = if (filled) SociableTheme.onAccent else SociableTheme.ink,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
