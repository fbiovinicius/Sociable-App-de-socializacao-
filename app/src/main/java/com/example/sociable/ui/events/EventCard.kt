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
import com.example.sociable.ui.theme.OrangeSoft
import com.example.sociable.ui.theme.Plum
import com.example.sociable.ui.theme.SCream
import com.example.sociable.ui.theme.SInk

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StickerBox(
        modifier = modifier.fillMaxWidth(),
        fill = if (event.isSecret) OrangeSoft else SCream,
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
                        color = Plum,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${formatEventDay(event.startAt)} · ${ManausTime.time(event.startAt)}",
                        color = Plum,
                        fontSize = 12.sp,
                    )
                }
                Text(
                    text = formatPriceText(event.isFree, event.price),
                    color = SInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = event.title,
                color = SInk,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                text = if (event.isSecret) {
                    "📍 Local revelado 2 h antes para quem confirmar"
                } else {
                    "📍 ${event.venueName} · ${event.address}"
                },
                color = SInk,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "👥 ${formatSpots(event.goingCount, event.capacity)}",
                    color = SInk,
                    fontSize = 13.sp,
                )
                Text(
                    text = "Ver detalhes →",
                    color = SInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
