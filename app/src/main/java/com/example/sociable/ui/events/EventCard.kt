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
import com.example.sociable.ui.common.formatDistance
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
import androidx.compose.foundation.layout.size
import java.time.LocalDate
import com.example.sociable.ui.common.EmojiBadge

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Distância do aparelho até o evento, quando conhecida. */
    distanceKm: Double? = null,
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
                if (event.isSecret) {
                    Pin(pinRes(event.category, event.isSecret), size = 38.dp)
                } else {
                    EmojiBadge(emoji = event.emoji, size = 38.dp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (event.isSecret) {
                            "🔒 Chat da Cidade"
                        } else if (distanceKm != null) {
                            "${event.category.label} · ${formatDistance(distanceKm)}"
                        } else {
                            event.category.label
                        },
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

/** "hoje", "amanhã" ou o dia da semana com a data. */
private fun dayLabel(startAt: Instant): String {
    val today = LocalDate.now(ManausTime.ZONE)
    return when (ManausTime.localDate(startAt)) {
        today -> "hoje"
        today.plusDays(1) -> "amanhã"
        else -> formatEventDay(startAt)
    }
}

/**
 * Versão pequena do card, para o Descobrir: quadradinho com o ícone, título,
 * local · dia, hora, barra de vagas e preço à direita.
 */
@Composable
fun EventCardCompact(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Distância do aparelho até o evento, quando conhecida. */
    distanceKm: Double? = null,
) {
    val isNow = remember(event.id) { event.isHappeningAt(Instant.now()) }
    val capacity = event.capacity
    val place = if (event.isSecret) "🔒 Chat da Cidade" else event.venueName
    val details = listOfNotNull(
        place.takeIf { it.isNotBlank() },
        "${dayLabel(event.startAt)}, ${ManausTime.time(event.startAt)}",
        "${event.goingCount} confirmados".takeIf { capacity == null },
        distanceKm?.let { formatDistance(it) },
    ).joinToString(" · ")

    StickerBox(
        modifier = modifier.fillMaxWidth(),
        fill = if (event.isSecret) SociableTheme.accentSoft else SociableTheme.background,
        radius = 22.dp,
        shadow = 4.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val tileShape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(SociableTheme.accentSoft, tileShape)
                    .border(2.dp, SociableTheme.ink, tileShape),
                contentAlignment = Alignment.Center,
            ) {
                if (event.isSecret) {
                    Pin(pinRes(event.category, event.isSecret), size = 34.dp)
                } else {
                    EmojiBadge(emoji = event.emoji, size = 36.dp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    color = SociableTheme.ink,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = details,
                    color = SociableTheme.muted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (capacity != null && capacity > 0) {
                    val progress = (event.goingCount.toFloat() / capacity).coerceIn(0f, 1f)
                    val barShape = RoundedCornerShape(4.dp)
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .fillMaxWidth()
                            .height(7.dp)
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
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                if (isNow) {
                    StatusBadge(text = "Agora", filled = true)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = formatPriceText(event.isFree, event.price),
                    color = SociableTheme.ink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}