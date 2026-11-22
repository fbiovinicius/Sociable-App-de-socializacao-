package com.example.sociable.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.core.location.distanceKm
import com.example.sociable.data.model.DateFilter
import com.example.sociable.data.model.EventCategory
import com.example.sociable.feature.events.EventListViewModel
import com.example.sociable.ui.common.EmptyState
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.map.rememberUserLocation
import com.example.sociable.ui.theme.SociableTheme

@Composable
fun EventListScreen(
    viewModel: EventListViewModel,
    onEventClick: (String) -> Unit,
    onCreateClick: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    // Pede a localização uma vez para mostrar a distância em cada card.
    val userLocation by rememberUserLocation(requestPermission = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SociableTheme.background)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Descobrir",
                    color = SociableTheme.ink,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "O que tá rolando em Manaus?",
                    color = SociableTheme.muted,
                    fontSize = 14.sp,
                )
            }
            SociableChip(
                text = "+ Criar",
                selected = true,
                onClick = onCreateClick,
            )
        }

        OutlinedTextField(
            value = state.filter.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = { Text("O que você quer viver?") },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SociableTheme.ink,
                unfocusedBorderColor = SociableTheme.outlineSoft,
                focusedContainerColor = SociableTheme.surface,
                unfocusedContainerColor = SociableTheme.surface,
                cursorColor = SociableTheme.accent,
            ),
        )

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

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(EventCategory.entries) { category ->
                SociableChip(
                    text = category.label,
                    selected = category in state.filter.categories,
                    onClick = { viewModel.onCategoryToggle(category) },
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 24.dp, top = 12.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(items = state.events, key = { it.id }) { event ->
                        // Segredinho tem coordenada neutra no feed: sem distância.
                        val distance = userLocation?.takeIf { !event.isSecret }?.let { here ->
                            distanceKm(here.latitude, here.longitude, event.latitude, event.longitude)
                        }
                        EventCard(
                            event = event,
                            onClick = { onEventClick(event.id) },
                            distanceKm = distance,
                        )
                    }
                }
            }
        }
    }
}
