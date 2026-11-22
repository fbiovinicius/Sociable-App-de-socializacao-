package com.example.sociable.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sociable.ui.theme.SociableTheme

/**
 * Avatar com anel de progresso (ex.: "perfil 35% completo") e selo de porcentagem.
 * [progress] vai de 0f a 1f. O conteúdo (boneco/foto) entra centralizado.
 */
@Composable
fun ProgressRingAvatar(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 104.dp,
    showPercent: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val track = SociableTheme.outlineSoft
    val bar = SociableTheme.accent
    val safe = progress.coerceIn(0f, 1f)
    Box(modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 7.dp.toPx()
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )
            drawArc(
                color = bar,
                startAngle = -90f,
                sweepAngle = 360f * safe,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        content()
        if (showPercent) {
            val badge = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-6).dp, y = 2.dp)
                    .background(SociableTheme.surface, badge)
                    .border(2.dp, SociableTheme.ink, badge)
                    .padding(horizontal = 8.dp, vertical = 1.dp),
            ) {
                Text(
                    text = "${(safe * 100).toInt()}%",
                    color = SociableTheme.accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}