package com.example.sociable.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.core.time.ManausTime
import com.example.sociable.data.model.EventCategory
import com.example.sociable.feature.map.ManausMapProjection
import com.example.sociable.feature.map.MapMarker
import com.example.sociable.feature.map.MapViewModel
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.Pin
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.common.StickerBox
import com.example.sociable.ui.common.formatEventDay
import com.example.sociable.ui.common.formatPriceText
import com.example.sociable.ui.common.formatSpots
import com.example.sociable.ui.common.pinRes
import com.example.sociable.ui.theme.Coral
import com.example.sociable.ui.theme.Orange
import com.example.sociable.ui.theme.OrangeSoft
import com.example.sociable.ui.theme.Peach
import com.example.sociable.ui.theme.Plum
import com.example.sociable.ui.theme.Rose
import com.example.sociable.ui.theme.SCream
import com.example.sociable.ui.theme.SInk
import com.example.sociable.ui.theme.SSand
import kotlin.math.roundToInt

/* Mapa ilustrativo de Manaus (desenhado em Canvas). Quando houver chave de API,
   troque o conteúdo do Canvas pelo Google Maps Compose e use latitude/longitude nos pins. */

private data class MapLabelItem(
    val text: String,
    val x: Float,
    val y: Float,
    val minScale: Float,
    val big: Boolean,
)

private val mapLabels = listOf(
    MapLabelItem("Rio Negro", 0.40f, 0.045f, 1f, true),
    MapLabelItem("Ponta Negra", 0.03f, 0.22f, 1f, true),
    MapLabelItem("Centro", 0.43f, 0.35f, 1f, true),
    MapLabelItem("Adrianópolis", 0.56f, 0.62f, 1f, true),
    MapLabelItem("Parque 10", 0.14f, 0.66f, 1f, true),
    MapLabelItem("Parque do Mindu", 0.70f, 0.755f, 1.2f, true),
    MapLabelItem("Av. Constantino Nery", 0.03f, 0.575f, 1.6f, false),
    MapLabelItem("Av. Djalma Batista", 0.47f, 0.88f, 1.6f, false),
    MapLabelItem("Av. Eduardo Ribeiro", 0.08f, 0.875f, 1.6f, false),
    MapLabelItem("Praça da Matriz", 0.33f, 0.43f, 2f, false),
    MapLabelItem("Teatro Amazonas", 0.52f, 0.31f, 2f, false),
)

private val mapFilters = listOf(
    "Todos", "Agora", "Gratuitos", "Música", "Gastronomia", "Esporte", "Cultura",
)

private fun DrawScope.drawCity() {
    val w = size.width
    val h = size.height

    val cols = 8
    val rows = 12
    val cw = w / cols
    val ch = h / rows
    for (i in 0 until cols) for (j in 0 until rows) {
        val topLeft = Offset(i * cw + cw * 0.09f, j * ch + ch * 0.09f)
        val blockSize = Size(cw * 0.82f, ch * 0.82f)
        if ((i * 7 + j * 5) % 11 == 0) {
            drawRoundRect(
                color = OrangeSoft,
                topLeft = topLeft,
                size = blockSize,
                cornerRadius = CornerRadius(18f),
            )
        } else {
            drawRoundRect(
                color = SCream,
                topLeft = topLeft,
                size = blockSize,
                cornerRadius = CornerRadius(10f),
            )
            drawRoundRect(
                color = SSand.copy(alpha = 0.6f),
                topLeft = topLeft,
                size = blockSize,
                cornerRadius = CornerRadius(10f),
                style = Stroke(width = 2f),
            )
        }
    }

    val parkTopLeft = Offset(w * 0.70f, h * 0.775f)
    val parkSize = Size(w * 0.26f, h * 0.17f)
    drawRoundRect(
        color = OrangeSoft,
        topLeft = parkTopLeft,
        size = parkSize,
        cornerRadius = CornerRadius(60f),
    )
    drawRoundRect(
        color = Orange.copy(alpha = 0.6f),
        topLeft = parkTopLeft,
        size = parkSize,
        cornerRadius = CornerRadius(60f),
        style = Stroke(width = 4f),
    )
    for (n in 0 until 15) {
        drawCircle(
            color = Orange.copy(alpha = 0.45f),
            radius = 8f,
            center = Offset(w * 0.73f + (n % 5) * w * 0.045f, h * 0.80f + (n / 5) * h * 0.04f),
        )
    }

    val avenues = listOf(
        Offset(0f, h * 0.56f) to Offset(w, h * 0.50f),
        Offset(w * 0.46f, 0f) to Offset(w * 0.54f, h),
        Offset(0f, h * 0.92f) to Offset(w, h * 0.62f),
    )
    avenues.forEach { (a, b) ->
        drawLine(color = Peach, start = a, end = b, strokeWidth = 44f, cap = StrokeCap.Round)
        drawLine(color = SSand, start = a, end = b, strokeWidth = 34f, cap = StrokeCap.Round)
        drawLine(
            color = SCream,
            start = a,
            end = b,
            strokeWidth = 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f)),
        )
    }

    val river = Path().apply {
        moveTo(0f, 0f)
        lineTo(0f, h * 0.14f)
        cubicTo(w * 0.25f, h * 0.20f, w * 0.50f, h * 0.08f, w * 0.75f, h * 0.15f)
        cubicTo(w * 0.88f, h * 0.19f, w * 0.95f, h * 0.14f, w, h * 0.12f)
        lineTo(w, 0f)
        close()
    }
    val shore = Path().apply {
        moveTo(0f, h * 0.14f)
        cubicTo(w * 0.25f, h * 0.20f, w * 0.50f, h * 0.08f, w * 0.75f, h * 0.15f)
        cubicTo(w * 0.88f, h * 0.19f, w * 0.95f, h * 0.14f, w, h * 0.12f)
    }
    drawPath(river, color = Plum.copy(alpha = 0.25f))
    drawPath(shore, color = Plum.copy(alpha = 0.55f), style = Stroke(width = 5f))
}

private fun MapMarker.matchesFilter(filter: String): Boolean = when (filter) {
    "Agora" -> isHappeningNow
    "Gratuitos" -> isFree
    "Música" -> category == EventCategory.MUSIC
    "Gastronomia" -> category == EventCategory.FOOD
    "Esporte" -> category == EventCategory.SPORTS
    "Cultura" -> category in setOf(
        EventCategory.ART,
        EventCategory.ACADEMIC,
        EventCategory.RELIGION,
        EventCategory.OTHER,
    )
    else -> true
}

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onEventClick: (String) -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("Todos") }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val visible = state.markers.filter { marker ->
        marker.matchesFilter(filter) && (
            query.isBlank() ||
                marker.title.contains(query, ignoreCase = true) ||
                marker.venueName.contains(query, ignoreCase = true) ||
                marker.address.contains(query, ignoreCase = true)
            )
    }
    val selected = visible.firstOrNull { it.eventId == state.selectedEventId }
        ?: visible.firstOrNull()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Rose),
    ) {
        val wPx = constraints.maxWidth.toFloat()
        val hPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        fun clampOffset(o: Offset, s: Float) = Offset(
            o.x.coerceIn(wPx * (1f - s), 0f),
            o.y.coerceIn(hPx * (1f - s), 0f),
        )

        fun zoomBy(delta: Float) {
            val newScale = (scale + delta).coerceIn(1f, 3f)
            val k = newScale / scale
            val center = Offset(wPx / 2f, hPx / 2f)
            offset = clampOffset((offset - center) * k + center, newScale)
            scale = newScale
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(wPx, hPx) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(1f, 3f)
                        val k = newScale / scale
                        offset = clampOffset((offset - centroid) * k + centroid + pan, newScale)
                        scale = newScale
                    }
                },
        ) {
            withTransform({
                translate(offset.x, offset.y)
                scale(scale, scale, Offset.Zero)
            }) {
                drawCity()
            }
        }

        mapLabels.filter { scale >= it.minScale }.forEach { label ->
            Text(
                text = label.text,
                color = if (label.big) Plum else SInk.copy(alpha = 0.7f),
                fontSize = if (label.big) 11.sp else 9.sp,
                fontWeight = if (label.big) FontWeight.Bold else FontWeight.Medium,
                fontStyle = if (label.text == "Rio Negro") FontStyle.Italic else FontStyle.Normal,
                modifier = Modifier.offset {
                    IntOffset(
                        (label.x * wPx * scale + offset.x).roundToInt(),
                        (label.y * hPx * scale + offset.y).roundToInt(),
                    )
                },
            )
        }

        // Você está aqui
        val me = Offset(0.50f * wPx * scale + offset.x, 0.62f * hPx * scale + offset.y)
        val meSize = with(density) { 20.dp.toPx() }
        Box(
            modifier = Modifier
                .offset {
                    IntOffset((me.x - meSize / 2).roundToInt(), (me.y - meSize / 2).roundToInt())
                }
                .size(20.dp)
                .background(Coral.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Coral, CircleShape)
                    .border(2.dp, SCream, CircleShape),
            )
        }

        // Pins
        visible.sortedBy { it.eventId == selected?.eventId }.forEach { marker ->
            val isSelected = marker.eventId == selected?.eventId
            val pinSize = if (isSelected) 52.dp else 40.dp
            val pw = with(density) { pinSize.toPx() }
            val ph = pw * 58f / 48f
            val point = ManausMapProjection.project(marker.latitude, marker.longitude)
            val sx = point.x * wPx * scale + offset.x
            val sy = point.y * hPx * scale + offset.y
            Pin(
                id = pinRes(marker.category, isSecret = false),
                modifier = Modifier
                    .offset { IntOffset((sx - pw / 2f).roundToInt(), (sy - ph).roundToInt()) }
                    .clickable { viewModel.onMarkerSelected(marker.eventId) },
                size = pinSize,
            )
            if (scale >= 1.8f) {
                Text(
                    text = marker.title,
                    color = SInk,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .offset {
                            IntOffset((sx - pw).roundToInt(), (sy + 4f).roundToInt())
                        }
                        .background(SCream, RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                )
            }
        }

        // Busca e filtros
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("O que você quer viver?") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SInk,
                    unfocusedBorderColor = SInk,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(mapFilters) { item ->
                    SociableChip(
                        text = item,
                        selected = filter == item,
                        onClick = { filter = item },
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            SociableChip(
                text = "📍 ${visible.size} ${if (visible.size == 1) "rolê" else "rolês"} por aqui",
                selected = false,
                onClick = {},
            )
            if (state.isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = SInk,
                    strokeWidth = 2.dp,
                )
            }
            ErrorText(error = state.error)
        }

        // Controles de zoom
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MapButton("+") { zoomBy(0.5f) }
            MapButton("−") { zoomBy(-0.5f) }
            MapButton("◎") {
                scale = 1f
                offset = Offset.Zero
            }
        }

        // Cartão do evento selecionado
        StickerBox(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            fill = SCream,
            radius = 24.dp,
            contentPadding = PaddingValues(18.dp),
        ) {
            Column {
                if (selected != null) {
                    Text(
                        text = "${selected.category.label} · ${formatEventDay(selected.startAt)} · ${ManausTime.time(selected.startAt)}",
                        color = Plum,
                        fontSize = 12.sp,
                    )
                    Text(
                        text = selected.title,
                        color = SInk,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${formatPriceText(selected.isFree, selected.price)} · ${formatSpots(selected.goingCount, selected.capacity)}",
                        color = SInk,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = "📍 ${selected.venueName} · ${selected.address}",
                        color = Plum,
                        fontSize = 12.sp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SociableButton(
                        text = "Ver detalhes",
                        onClick = { onEventClick(selected.eventId) },
                    )
                } else {
                    Text(
                        text = "Nenhum rolê encontrado",
                        color = SInk,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Tente outro filtro ou outra busca.",
                        color = Plum,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun MapButton(text: String, onClick: () -> Unit) {
    StickerBox(
        modifier = Modifier.width(48.dp),
        fill = SCream,
        radius = 14.dp,
        shadow = 3.dp,
        height = 44.dp,
        contentAlignment = Alignment.Center,
        onClick = onClick,
    ) {
        Text(
            text = text,
            color = SInk,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}
