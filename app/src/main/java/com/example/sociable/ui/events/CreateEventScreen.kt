@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.sociable.ui.events

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.core.time.ManausTime
import com.example.sociable.data.model.EventCategory
import com.example.sociable.feature.events.CreateEventViewModel
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.FormTextField
import com.example.sociable.ui.common.PrimaryButton
import com.example.sociable.ui.common.SectionTitle
import com.example.sociable.ui.common.SociableTopBar
import com.example.sociable.ui.common.message

@Composable
fun CreateEventScreen(
    viewModel: CreateEventViewModel,
    onCreated: (String) -> Unit,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(state.createdEventId) {
        state.createdEventId?.let(onCreated)
    }

    Scaffold(
        topBar = { SociableTopBar(title = "Criar evento", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionTitle(text = "Informações")

            FormTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = "Título do evento",
                error = state.titleError,
            )

            FormTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "Descrição",
                error = state.descriptionError,
                singleLine = false,
                minLines = 3,
            )

            Text(text = "Categoria", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EventCategory.entries.forEach { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { viewModel.onCategoryChange(category) },
                        label = { Text(category.label) },
                    )
                }
            }

            SectionTitle(text = "Local", modifier = Modifier.padding(top = 8.dp))

            FormTextField(
                value = state.venueName,
                onValueChange = viewModel::onVenueChange,
                label = "Nome do local",
                error = state.venueError,
            )

            FormTextField(
                value = state.address,
                onValueChange = viewModel::onAddressChange,
                label = "Endereço",
                error = state.addressError,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormTextField(
                    value = state.latitude,
                    onValueChange = viewModel::onLatitudeChange,
                    label = "Latitude",
                    error = state.latitudeError,
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f),
                )
                FormTextField(
                    value = state.longitude,
                    onValueChange = viewModel::onLongitudeChange,
                    label = "Longitude",
                    error = state.longitudeError,
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f),
                )
            }

            SectionTitle(text = "Quando", modifier = Modifier.padding(top = 8.dp))

            PeriodRow(
                label = "Início",
                value = ManausTime.dateTime(state.startAt),
                onMinus = { viewModel.onStartShift(-1) },
                onPlus = { viewModel.onStartShift(1) },
            )
            PeriodRow(
                label = "Fim",
                value = ManausTime.dateTime(state.endAt),
                onMinus = { viewModel.onEndShift(-1) },
                onPlus = { viewModel.onEndShift(1) },
            )
            val periodError = state.periodError
            if (periodError != null) {
                Text(
                    text = periodError.message(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            SectionTitle(text = "Ingresso", modifier = Modifier.padding(top = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "Evento gratuito")
                Switch(checked = state.isFree, onCheckedChange = viewModel::onFreeChange)
            }

            if (!state.isFree) {
                FormTextField(
                    value = state.price,
                    onValueChange = viewModel::onPriceChange,
                    label = "Preço (R$)",
                    error = state.priceError,
                    keyboardType = KeyboardType.Decimal,
                )
            }

            FormTextField(
                value = state.capacity,
                onValueChange = viewModel::onCapacityChange,
                label = "Capacidade (opcional)",
                error = state.capacityError,
                keyboardType = KeyboardType.Number,
            )

            FormTextField(
                value = state.imageUrl,
                onValueChange = viewModel::onImageUrlChange,
                label = "URL da imagem (opcional)",
                error = state.imageUrlError,
                keyboardType = KeyboardType.Uri,
            )

            ErrorText(error = state.error)

            Spacer(modifier = Modifier.height(4.dp))

            PrimaryButton(
                text = "Publicar evento",
                onClick = viewModel::onSubmit,
                isLoading = state.isLoading,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PeriodRow(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.9f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(2.4f),
        )
        OutlinedButton(onClick = onMinus) { Text("-1h") }
        OutlinedButton(onClick = onPlus) { Text("+1h") }
    }
}
