package com.example.sociable.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sociable.ui.theme.Coral
import com.example.sociable.ui.theme.SCream
import com.example.sociable.ui.theme.SInk
import com.example.sociable.ui.theme.Peach
import com.example.sociable.ui.theme.Plum
import com.example.sociable.ui.theme.Rose
import com.example.sociable.ui.theme.SSand

/** Base do visual "adesivo": contorno roxo-noite + sombra sólida deslocada. */
@Composable
fun StickerBox(
    modifier: Modifier = Modifier,
    fill: Color = Coral,
    radius: Dp = 28.dp,
    shadow: Dp = 4.dp,
    height: Dp? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentAlignment: Alignment = Alignment.TopStart,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Box(modifier.padding(end = shadow, bottom = shadow)) {
        Box(Modifier.matchParentSize().offset(shadow, shadow).background(SInk, shape))
        Box(
            Modifier
                .fillMaxWidth()
                .let { if (height != null) it.height(height) else it }
                .background(fill, shape)
                .border(2.5.dp, SInk, shape)
                .clip(shape)
                .let { if (onClick != null) it.clickable(onClick = onClick) else it }
                .padding(contentPadding),
            contentAlignment = contentAlignment,
            content = content,
        )
    }
}

@Composable
fun SociableButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondary: Boolean = false,
    enabled: Boolean = true,
) {
    StickerBox(
        modifier = modifier.fillMaxWidth(),
        fill = when { !enabled -> Peach; secondary -> SCream; else -> Coral },
        height = 56.dp,
        contentAlignment = Alignment.Center,
        onClick = if (enabled) onClick else null,
    ) {
        Text(text, color = if (secondary) SInk else SCream, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun SociableChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier
            .clip(shape)
            .background(if (selected) Coral else Rose, shape)
            .border(2.dp, SInk, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(text, color = if (selected) SCream else SInk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun SociableField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        isError = isError,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Coral,
            unfocusedBorderColor = SSand,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedLabelColor = Coral,
            unfocusedLabelColor = Plum,
            focusedTextColor = SInk,
            unfocusedTextColor = SInk,
        ),
    )
}

/** Pino de mapa com carinha (vetores ic_pin_*). */
@Composable
fun Pin(@DrawableRes id: Int, modifier: Modifier = Modifier, size: Dp = 48.dp) {
    Image(
        painter = painterResource(id),
        contentDescription = null,
        modifier = modifier.size(size, size * 58f / 48f),
    )
}