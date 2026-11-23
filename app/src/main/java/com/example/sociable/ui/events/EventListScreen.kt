package com.example.sociable.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.R
import com.example.sociable.core.location.distanceKm
import com.example.sociable.data.model.DateFilter
import com.example.sociable.data.model.Event
import com.example.sociable.data.model.EventCategory
import com.example.sociable.feature.events.EventListViewModel
import com.example.sociable.ui.common.EmptyState
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.common.SociableTextLink
import com.example.sociable.ui.common.StickerBox
import com.example.sociable.ui.map.rememberUserLocation
import com.example.sociable.ui.theme.SociableTheme

/** Quantos rolês aparecem no Descobrir antes do "Ver todos". */
private const val MAX_EM_ALTA = 4

@Composable
fun EventListScreen(
    viewModel: EventListViewModel,
    onEventClick: (String) -> Unit,
    onCreateClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        EventListContent(viewModel = viewModel, onEventClick = onEventClick)
        CreateFab(
            onClick = onCreateClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
        )
    }
}

@Composable
private fun EventListContent(
    viewModel: EventListViewModel,
    onEventClick: (String) -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    // Pede a localização uma vez para mostrar a distância em cada card.
    val userLocation by rememberUserLocation(requestPermission = true)
    var showFilters by remember { mutableStateOf(false) }

    if (showFilters) {
        CategoryFilterDialog(
            selected = state.filter.categories,
            onToggle = viewModel::onCategoryToggle,
            onClear = { state.filter.categories.forEach(viewModel::onCategoryToggle) },
            onDismiss = { showFilters = false },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SociableTheme.background)
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp)) {
            Text(
                text = "Descobrir",
                color = SociableTheme.ink,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "O que tá rolando em Manaus?",
                color = SociableTheme.muted,
                fontSize = 12.sp,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.filter.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("O que você quer viver?") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SociableTheme.ink,
                    unfocusedBorderColor = SociableTheme.outlineSoft,
                    focusedContainerColor = SociableTheme.surface,
                    unfocusedContainerColor = SociableTheme.surface,
                    cursorColor = SociableTheme.accent,
                ),
            )
            Spacer(modifier = Modifier.width(10.dp))
            val hasCategories = state.filter.categories.isNotEmpty()
            StickerBox(
                modifier = Modifier.size(56.dp),
                fill = if (hasCategories) SociableTheme.accent else SociableTheme.chip,
                radius = 16.dp,
                shadow = 3.dp,
                contentAlignment = Alignment.Center,
                onClickLabel = "Filtrar por categoria",
                onClick = { showFilters = true },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_filter),
                    contentDescription = "Filtros",
                    tint = if (hasCategories) SociableTheme.onAccent else SociableTheme.ink,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(DateFilter.entries) { dateFilter ->
                SociableChip(
                    text = dateFilter.label,
                    selected = state.filter.dateFilter == dateFilter,
                    onClick = { viewModel.onDateFilterChange(dateFilter) },
                )
            }
            item {
                SociableChip(
                    text = "Grátis",
                    selected = state.filter.onlyFree,
                    onClick = { viewModel.onOnlyFreeChange(!state.filter.onlyFree) },
                )
            }
        }

        ErrorText(
            error = state.error,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        when {
            state.isLoading && state.events.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = SociableTheme.ink)
                }
            }

            state.events.isEmpty() -> {
                EmptyState(
                    title = "Nenhum evento encontrado",
                    subtitle = "Tente mudar os filtros ou criar o seu próprio rolê.",
                    modifier = Modifier.padding(top = 48.dp),
                )
            }

            else -> {
                var showAll by rememberSaveable { mutableStateOf(false) }
                // "Em alta" = os com mais gente confirmada. Só vale sem busca/filtro.
                val emAlta = remember(state.events) {
                    state.events.sortedWith(
                        compareByDescending<Event> { it.goingCount }.thenBy { it.startAt },
                    )
                }
                val limited = state.filter.isEmpty && !showAll && emAlta.size > MAX_EM_ALTA
                val shown = when {
                    limited -> emAlta.take(MAX_EM_ALTA)
                    state.filter.isEmpty -> emAlta
                    else -> state.events
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 24.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Text(
                            text = when {
                                limited -> "🔥 Em alta"
                                shown.size == 1 -> "1 rolê por aqui"
                                else -> "${shown.size} rolês por aqui"
                            },
                            color = SociableTheme.muted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    items(items = shown, key = { it.id }) { event ->
                        // Segredinho tem coordenada neutra no feed: sem distância.
                        val distance = userLocation?.takeIf { !event.isSecret }?.let { here ->
                            distanceKm(here.latitude, here.longitude, event.latitude, event.longitude)
                        }
                        EventCardCompact(
                            event = event,
                            onClick = { onEventClick(event.id) },
                            distanceKm = distance,
                        )
                    }
                    if (limited) {
                        item {
                            SociableTextLink(
                                text = "Ver todos os ${emAlta.size} rolês →",
                                onClick = { showAll = true },
                                color = SociableTheme.accent,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Janela do botão de filtros: escolher uma ou mais categorias. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryFilterDialog(
    selected: Set<EventCategory>,
    onToggle: (EventCategory) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        StickerBox(
            modifier = Modifier.fillMaxWidth(),
            fill = SociableTheme.background,
            radius = 24.dp,
            contentPadding = PaddingValues(18.dp),
        ) {
            Column {
                Text(
                    text = "Filtrar por categoria",
                    color = SociableTheme.ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EventCategory.entries.forEach { category ->
                        SociableChip(
                            text = category.label,
                            selected = category in selected,
                            onClick = { onToggle(category) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SociableButton(
                        text = "Limpar",
                        onClick = onClear,
                        secondary = true,
                        enabled = selected.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                    )
                    SociableButton(
                        text = "Pronto",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** Botão "+" redondo no canto inferior direito, no mesmo estilo adesivo dos outros botões. */
@Composable
private fun CreateFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shadowColor = SociableTheme.shadow
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .drawBehind {
                drawCircle(
                    color = shadowColor,
                    radius = size.minDimension / 2,
                    center = Offset(center.x + 4.dp.toPx(), center.y + 4.dp.toPx()),
                )
            }
            .border(3.dp, SociableTheme.ink, CircleShape),
        shape = CircleShape,
        containerColor = SociableTheme.accent,
        contentColor = SociableTheme.onAccent,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Criar evento",
            modifier = Modifier.size(30.dp),
        )
    }
}