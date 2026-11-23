package com.example.sociable.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sociable.ui.theme.SociableTheme

/** Marcador redondo com o emoji do evento e uma pontinha apontando para o local. */
@Composable
fun EmojiPin(
    emoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    highlighted: Boolean = false,
    showTip: Boolean = true,
    contentDescription: String? = null,
) {
    val ink = SociableTheme.ink
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }
    Column(
        modifier = modifier.then(semanticsModifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(
                    if (highlighted) SociableTheme.accentSoft else SociableTheme.surface,
                    CircleShape,
                )
                .border(
                    width = if (highlighted) 3.dp else 2.5.dp,
                    color = if (highlighted) SociableTheme.accent else ink,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = emoji, fontSize = (size.value * 0.52f).sp)
        }
        if (showTip) {
            Canvas(
                modifier = Modifier
                    .offset(y = (-1).dp)
                    .size(width = 12.dp, height = 8.dp),
            ) {
                val w = this.size.width
                val h = this.size.height
                val tip = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w / 2f, h)
                    close()
                }
                drawPath(tip, color = ink)
            }
        }
    }
}