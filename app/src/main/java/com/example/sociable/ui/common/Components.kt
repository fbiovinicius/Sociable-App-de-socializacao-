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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sociable.R
import com.example.sociable.core.AppError
import com.example.sociable.ui.theme.SociableTheme

/**
 * Base do visual "adesivo": contorno roxo-noite + sombra sólida deslocada.
 *
 * [minHeight] mantém o alvo de toque de no mínimo 48 dp sem impedir que o
 * conteúdo cresça quando a pessoa aumenta a fonte do sistema.
 */
@Composable
fun StickerBox(
    modifier: Modifier = Modifier,
    fill: Color = SociableTheme.accent,
    radius: Dp = 28.dp,
    shadow: Dp = 4.dp,
    height: Dp? = null,
    minHeight: Dp? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentAlignment: Alignment = Alignment.TopStart,
    onClickLabel: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Box(modifier.padding(end = shadow, bottom = shadow)) {
        Box(
            Modifier
                .matchParentSize()
                .offset(shadow, shadow)
                .background(SociableTheme.shadow, shape)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .let { if (height != null) it.height(height) else it }
                .let { if (minHeight != null) it.heightIn(min = minHeight) else it }
                .background(fill, shape)
                .border(2.5.dp, SociableTheme.ink, shape)
                .clip(shape)
                .let { base ->
                    if (onClick != null) {
                        base.clickable(
                            onClickLabel = onClickLabel,
                            role = Role.Button,
                            onClick = onClick,
                        )
                    } else {
                        base
                    }
                }
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
    val interactive = enabled && !isLoading
    StickerBox(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                if (!interactive) disabled()
            },
        fill = when {
            !enabled || isLoading -> SociableTheme.peach
            secondary -> SociableTheme.chip
            else -> SociableTheme.accent
        },
        minHeight = 56.dp,
        contentAlignment = Alignment.Center,
        onClick = if (interactive) onClick else null,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = SociableTheme.ink,
                strokeWidth = 2.5.dp,
            )
        } else {
            Text(
                text = text,
                color = if (secondary || !enabled) SociableTheme.ink else SociableTheme.onAccent,
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
            .background(if (selected) SociableTheme.accent else SociableTheme.chip, shape)
            .border(2.dp, SociableTheme.ink, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.selected = selected }
            .heightIn(min = 48.dp)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (selected) SociableTheme.onAccent else SociableTheme.ink,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
    }
}

/** Pino de mapa com carinha (vetores ic_pin_*). Decorativo por padrão. */
@Composable
fun Pin(
    @DrawableRes id: Int,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    contentDescription: String? = null,
) {
    Image(
        painter = painterResource(id),
        contentDescription = contentDescription,
        modifier = modifier.size(size, size * 58f / 48f),
    )
}

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier, sub: String? = null) {
    Column(modifier) {
        Text(
            text = text,
            color = SociableTheme.ink,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.semantics { heading() },
        )
        if (sub != null) {
            Text(
                text = sub,
                color = SociableTheme.muted,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
        }
    }
}

@Composable
fun BackLink(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clickable(
                onClickLabel = "Voltar",
                role = Role.Button,
                onClick = onBack,
            )
            .semantics { contentDescription = "Voltar" },
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = "← Voltar",
            color = SociableTheme.muted,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * Link de texto com alvo de toque de 48 dp (usado em "Esqueci a senha",
 * "Já tenho conta", "Agora não" etc.).
 */
@Composable
fun SociableTextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = SociableTheme.muted,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .wrapContentHeight(Alignment.CenterVertically),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
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
            focusedBorderColor = SociableTheme.accent,
            unfocusedBorderColor = SociableTheme.outlineSoft,
            errorBorderColor = SociableTheme.accent,
            focusedContainerColor = SociableTheme.surface,
            unfocusedContainerColor = SociableTheme.surface,
            errorContainerColor = SociableTheme.surface,
            focusedLabelColor = SociableTheme.accent,
            unfocusedLabelColor = SociableTheme.muted,
            errorLabelColor = SociableTheme.accent,
            focusedTextColor = SociableTheme.ink,
            unfocusedTextColor = SociableTheme.ink,
            errorTextColor = SociableTheme.ink,
            cursorColor = SociableTheme.accent,
        ),
    )
}

@Composable
fun ErrorText(error: AppError?, modifier: Modifier = Modifier) {
    if (error != null) {
        Text(
            text = error.message(),
            color = SociableTheme.accent,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
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
            color = SociableTheme.ink,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = subtitle,
            color = SociableTheme.muted,
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
        color = SociableTheme.ink,
        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier.semantics { heading() },
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
            color = SociableTheme.muted,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(92.dp),
        )
        Text(
            text = value,
            color = SociableTheme.ink,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
        )
    }
}
