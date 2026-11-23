package com.example.sociable.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sociable.ui.common.EmojiPin
import com.example.sociable.ui.common.FormTextField
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.theme.SociableTheme

/** Grupo de emojis do seletor. [keywords] alimenta a busca por texto. */
data class EmojiGroup(
    val label: String,
    val keywords: String,
    val emojis: List<String>,
) {
    fun matches(query: String): Boolean {
        val q = query.trim()
        return q.isNotEmpty() &&
                (label.contains(q, ignoreCase = true) || keywords.contains(q, ignoreCase = true))
    }
}

val EMOJI_GROUPS = listOf(
    EmojiGroup(
        "Festa", "balada aniversario comemorar drink bar luau",
        listOf("🎉", "🎊", "🥳", "🪩", "🍻", "🍹", "🍾", "🎈", "🎂", "🕺", "💃", "🎤", "🎧", "🔥", "✨", "🌙"),
    ),
    EmojiGroup(
        "Música", "show banda jazz samba cantar festival",
        listOf("🎶", "🎵", "🎸", "🎷", "🥁", "🎹", "🎺", "🎻", "🎤", "🎧", "🎭", "🎬"),
    ),
    EmojiGroup(
        "Comida", "gastronomia restaurante feira cafe churrasco bebida",
        listOf("🍕", "🍔", "🍣", "🍦", "☕", "🍺", "🥘", "🍽️", "🌮", "🍰", "🍉", "🥟", "🍷", "🥩"),
    ),
    EmojiGroup(
        "Esporte", "corrida futebol treino academia bike yoga",
        listOf("⚽", "🏀", "🏐", "🏃", "🚴", "🏊", "🧘", "🏋️", "🥊", "🎾", "🛹", "🏓"),
    ),
    EmojiGroup(
        "Natureza", "parque ao ar livre trilha praia rio barco",
        listOf("🌳", "🌴", "🌊", "🏖️", "⛵", "🚣", "🏕️", "🥾", "🌅", "🌺", "🦜", "🐟"),
    ),
    EmojiGroup(
        "Jogos", "tecnologia games tabuleiro nerd hackathon",
        listOf("🎮", "🕹️", "🎲", "♟️", "🧩", "💻", "🤖", "🚀", "📱", "🔬"),
    ),
    EmojiGroup(
        "Arte", "cultura teatro cinema sarau livro estudo workshop",
        listOf("🎨", "🖌️", "📚", "📖", "🎭", "🎬", "📷", "🧵", "✍️", "🎓", "💡", "🙏"),
    ),
)

/** Botão quadrado com o emoji atual e um "+" no canto. Abre o seletor. */
@Composable
fun EmojiPickerButton(
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .size(56.dp)
            .clickable(
                onClickLabel = "Escolher emoji do evento",
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(SociableTheme.accentSoft)
                .border(2.5.dp, SociableTheme.ink, shape),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = emoji, fontSize = 28.sp)
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 7.dp, y = 7.dp)
                .size(22.dp)
                .background(SociableTheme.accent, CircleShape)
                .border(2.dp, SociableTheme.ink, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = SociableTheme.onAccent,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

/** Bottom sheet para escolher o emoji que vai aparecer no mapa da cidade. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPickerSheet(
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var pending by remember { mutableStateOf(selected) }
    var query by remember { mutableStateOf("") }
    var groupIndex by remember { mutableIntStateOf(0) }

    val emojis = if (query.isBlank()) {
        EMOJI_GROUPS[groupIndex].emojis
    } else {
        EMOJI_GROUPS.filter { it.matches(query) }.flatMap { it.emojis }.distinct()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SociableTheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text = "Escolha o emoji do evento",
                color = SociableTheme.ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.height(10.dp))
            FormTextField(
                value = query,
                onValueChange = { query = it },
                label = "Buscar emoji (ex: festa, comida)",
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(EMOJI_GROUPS.indices.toList()) { index ->
                    SociableChip(
                        text = EMOJI_GROUPS[index].label,
                        selected = query.isBlank() && index == groupIndex,
                        onClick = {
                            query = ""
                            groupIndex = index
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (emojis.isEmpty()) {
                Text(
                    text = "Nenhum emoji encontrado",
                    color = SociableTheme.muted,
                    modifier = Modifier
                        .height(220.dp)
                        .padding(top = 12.dp),
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(220.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(emojis) { emoji ->
                        val isSelected = emoji == pending
                        val shape = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(shape)
                                .background(if (isSelected) SociableTheme.accentSoft else SociableTheme.surface)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) SociableTheme.accent else SociableTheme.chip,
                                    shape = shape,
                                )
                                .clickable(role = Role.Button) { pending = emoji },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = emoji, fontSize = 26.sp)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmojiPin(emoji = pending, size = 40.dp, showTip = false)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Assim ele aparece no mapa da cidade",
                    color = SociableTheme.muted,
                    fontSize = 12.sp,
                )
            }
            SociableButton(
                text = "Usar este emoji",
                onClick = { onSelect(pending) },
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}