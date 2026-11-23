package com.example.sociable.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sociable.ui.theme.SociableTheme

/** Quadradinho com o emoji do evento, usado nos cards. */
@Composable
fun EmojiBadge(
    emoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(size)
            .background(SociableTheme.accentSoft, shape)
            .border(2.dp, SociableTheme.ink, shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = (size.value * 0.55f).sp)
    }
}