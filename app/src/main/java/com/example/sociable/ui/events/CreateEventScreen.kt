package com.example.sociable.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.core.time.ManausTime
import com.example.sociable.data.model.EventCategory
import com.example.sociable.feature.events.CreateEventViewModel
import com.example.sociable.ui.common.BackLink
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.FormTextField
import com.example.sociable.ui.common.ScreenTitle
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.common.StickerBox
import com.example.sociable.ui.common.message
import com.example.sociable.ui.theme.SociableTheme
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateEventScreen(
    viewModel: CreateEventViewModel,
    startAsSecret: Boolean = false,
    onCreated: (String) -> Unit,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        LocationPickerDialog(
            initial = state.latitude.toDoubleOrNull()?.let { lat ->
                state.longitude.toDoubleOrNull()?.let { lng -> LatLng(lat, lng) }
            },
            onDismiss = { showPicker = false },
            onConfirm = { point ->
                showPicker = false
                viewModel.onLocationPicked(point.latitude, point.longitude)
                // Preenche o endereço só se a pessoa ainda não digitou nada.
                if (state.address.isBlank()) {
                    scope.launch {
                        context.addressFor(point.latitude, point.longitude)?.let { found ->
                            viewModel.onAddressIfBlank(found)
                        }
                    }
                }
            },
        )
    }

    LaunchedEffect(startAsSecret) {
        if (startAsSecret) viewModel.onSecretChange(true)
    }

    LaunchedEffect(state.createdEventId) {
        state.createdEventId?.let(onCreated)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SociableTheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 28.dp, bottom = 24.dp),
    ) {
        BackLink(onBack)
        Spacer(modifier = Modifier.height(8.dp))
        ScreenTitle("Criar evento", sub = "Conte o que vai rolar")

        FormTextField(
            value = state.title,
            onValueChange = viewModel::onTitleChange,
            label = "Nome do evento",
            error = state.titleError,
        )
        Spacer(modifier = Modifier.height(12.dp))

        FormTextField(
            value = state.description,
            onValueChange = viewModel::onDescriptionChange,
            label = "Descrição",
            error = state.descriptionError,
            singleLine = false,
            minLines = 3,
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Tipo", color = SociableTheme.ink, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EventCategory.entries.forEach { category ->
                SociableChip(
                    text = category.label,
                    selected = state.category == category,
                    onClick = { viewModel.onCategoryChange(category) },
                )
            }
            SociableChip(
                text = "🔒 Segredinho",
                selected = state.isSecret,
                onClick = { viewModel.onSecretChange(!state.isSecret) },
            )
        }
        if (state.isSecret) {
            Spacer(modifier = Modifier.height(10.dp))
            StickerBox(
                modifier = Modifier.fillMaxWidth(),
                fill = SociableTheme.accentSoft,
                radius = 14.dp,
                contentPadding = PaddingValues(14.dp),
            ) {
                Text(
                    text = "O local fica oculto no feed e só é revelado aos convidados 2 h antes.",
                    color = SociableTheme.ink,
                    fontSize = 13.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Local", color = SociableTheme.ink, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        FormTextField(
            value = state.venueName,
            onValueChange = viewModel::onVenueChange,
            label = "Nome do local",
            error = state.venueError,
        )
        Spacer(modifier = Modifier.height(12.dp))
        FormTextField(
            value = state.address,
            onValueChange = viewModel::onAddressChange,
            label = if (state.isSecret) "Endereço (revelado aos convidados)" else "Endereço",
            error = state.addressError,
        )
        Spacer(modifier = Modifier.height(12.dp))
        val pickedPoint = state.latitude.toDoubleOrNull()?.let { lat ->
            state.longitude.toDoubleOrNull()?.let { lng -> LatLng(lat, lng) }
        }
        SociableChip(
            text = if (pickedPoint == null) "📍 Escolher no mapa" else "📍 Local marcado · mudar no mapa",
            selected = pickedPoint != null,
            onClick = { showPicker = true },
        )
        if (state.latitudeError != null || state.longitudeError != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Toque em \"Escolher no mapa\" para marcar o local do evento.",
                color = SociableTheme.ink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Quando", color = SociableTheme.ink, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        StickerBox(
            modifier = Modifier.fillMaxWidth(),
            fill = SociableTheme.chip,
            radius = 18.dp,
            contentPadding = PaddingValues(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        color = SociableTheme.ink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Valor", color = SociableTheme.ink, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SociableChip(
                text = "Gratuito",
                selected = state.isFree,
                onClick = { viewModel.onFreeChange(true) },
            )
            SociableChip(
                text = "Pago",
                selected = !state.isFree,
                onClick = { viewModel.onFreeChange(false) },
            )
        }
        if (!state.isFree) {
            Spacer(modifier = Modifier.height(12.dp))
            FormTextField(
                value = state.price,
                onValueChange = viewModel::onPriceChange,
                label = "Valor em reais",
                error = state.priceError,
                keyboardType = KeyboardType.Decimal,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        FormTextField(
            value = state.capacity,
            onValueChange = viewModel::onCapacityChange,
            label = "Limite de pessoas (opcional)",
            error = state.capacityError,
            keyboardType = KeyboardType.Number,
        )
        Spacer(modifier = Modifier.height(12.dp))
        FormTextField(
            value = state.imageUrl,
            onValueChange = viewModel::onImageUrlChange,
            label = "URL da imagem (opcional)",
            error = state.imageUrlError,
            keyboardType = KeyboardType.Uri,
        )

        ErrorText(error = state.error)
        Spacer(modifier = Modifier.height(20.dp))

        SociableButton(
            text = "Publicar evento",
            onClick = viewModel::onSubmit,
            isLoading = state.isLoading,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PeriodRow(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    Column {
        Text(
            text = label,
            color = SociableTheme.muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = value,
            color = SociableTheme.ink,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SociableChip(text = "−1h", selected = false, onClick = onMinus)
            SociableChip(text = "+1h", selected = false, onClick = onPlus)
        }
    }
}
