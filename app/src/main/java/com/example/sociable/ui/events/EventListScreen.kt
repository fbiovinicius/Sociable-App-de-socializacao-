@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sociable.ui.events

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.data.model.DateFilter
import com.example.sociable.data.model.EventCategory
import com.example.sociable.feature.events.EventListViewModel
import com.example.sociable.ui.common.EmptyState
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.SociableTopBar

@Composable
fun EventListScreen(
    viewModel: EventListViewModel,
    onEventClick: (String) -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMapClick: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            SociableTopBar(
                title = "Descobrir",
                actions = {
                    TextButton(onClick = onMapClick) { Text("Mapa") }
                    TextButton(onClick = onProfileClick) { Text("Perfil") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Criar evento")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = state.filter.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Buscar por nome, local...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DateFilter.entries.forEach { dateFilter ->
                    FilterChip(
                        selected = state.filter.dateFilter == dateFilter,
                        onClick = { viewModel.onDateFilterChange(dateFilter) },
                        label = { Text(dateFilter.label) },
                    )
                }
                FilterChip(
                    selected = state.filter.onlyFree,
                    onClick = { viewModel.onOnlyFreeChange(!state.filter.onlyFree) },
                    label = { Text("Grátis") },
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EventCategory.entries.forEach { category ->
                    FilterChip(
                        selected = category in state.filter.categories,
                        onClick = { viewModel.onCategoryToggle(category) },
                        label = { Text(category.label) },
                    )
                }
            }

            ErrorText(error = state.error)

            when {
                state.isLoading && state.events.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
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
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(items = state.events, key = { it.id }) { event ->
                            EventCard(
                                event = event,
                                onClick = { onEventClick(event.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
