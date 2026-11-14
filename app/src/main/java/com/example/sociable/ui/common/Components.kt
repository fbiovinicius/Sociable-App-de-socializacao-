package com.example.sociable.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sociable.R
import com.example.sociable.core.AppError
import com.example.sociable.ui.theme.Coral
import com.example.sociable.ui.theme.Peach
import com.example.sociable.ui.theme.Plum
import com.example.sociable.ui.theme.Rose
import com.example.sociable.ui.theme.SCream
import com.example.sociable.ui.theme.SInk
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
    isLoading: Boolean = false,
) {
    StickerBox(
        modifier = modifier.fillMaxWidth(),
        fill = when {
            !enabled || isLoading -> Peach
            secondary -> SCream
            else -> Coral
        },
        height = 56.dp,
        contentAlignment = Alignment.Center,
        onClick = if (enabled && !isLoading) onClick else null,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = SInk,
                strokeWidth = 2.5.dp,
            )
        } else {
            Text(
                text = text,
                color = if (secondary || !enabled) SInk else SCream,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
fun SociableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier
            .clip(shape)
            .background(if (selected) Coral else Rose, shape)
            .border(2.dp, SInk, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(
            text = text,
            color = if (selected) SCream else SInk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
    }
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

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier, sub: String? = null) {
    Column(modifier) {
        Text(text = text, color = SInk, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        if (sub != null) {
            Text(
                text = sub,
                color = Plum,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
        }
    }
}

@Composable
fun BackLink(onBack: () -> Unit) {
    Text(
        text = "← Voltar",
        color = Plum,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clickable(onClick = onBack)
            .padding(vertical = 8.dp),
    )
}

@Composable
fun SociableLogo(size: Dp = 132.dp) {
    Image(
        painter = painterResource(R.drawable.logo_sociable),
        contentDescription = "Logo do Sociable",
        modifier = Modifier
            .size(size)
            .clip(CircleShape),
    )
}

/** Campo de texto do Sociable, com teclado, máscara e mensagem de apoio. */
@Composable
fun SociableTextField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    minLines: Int = 1,
    supportingText: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        supportingText = if (supportingText != null) {
            { Text(supportingText) }
        } else {
            null
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Coral,
            unfocusedBorderColor = SSand,
            errorBorderColor = Coral,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            errorContainerColor = Color.White,
            focusedLabelColor = Coral,
            unfocusedLabelColor = Plum,
            errorLabelColor = Coral,
            focusedTextColor = SInk,
            unfocusedTextColor = SInk,
            errorTextColor = SInk,
            cursorColor = Coral,
        ),
    )
}

@Composable
fun ErrorText(error: AppError?, modifier: Modifier = Modifier) {
    if (error != null) {
        Text(
            text = error.message(),
            color = Coral,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            color = SInk,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = subtitle,
            color = Plum,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = SInk,
        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            color = Plum,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(92.dp),
        )
        Text(
            text = value,
            color = SInk,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
        )
    }
}
