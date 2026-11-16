package com.example.sociable.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.R
import com.example.sociable.core.time.ManausTime
import com.example.sociable.data.model.EventCategory
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
import com.example.sociable.ui.theme.SociableTheme
import com.example.sociable.ui.theme.isSociableDarkTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

/* Mapa do Google (Maps Compose). A chave de API vem de MAPS_API_KEY em local.properties
   e é injetada no AndroidManifest pelo build.gradle.kts do módulo app. */

private val MANAUS_CENTER = LatLng(-3.12, -60.01)
private const val DEFAULT_ZOOM = 12f

private val mapFilters = listOf(
    "Todos", "Agora", "Gratuitos", "Música", "Gastronomia", "Esporte", "Cultura",
)

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

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // No modo escuro o mapa também fica escuro; sem estilo, ele continuaria claro.
    // Usa o mesmo critério do tema do app (Perfil → Aparência), não só o do sistema.
    val darkTheme = isSociableDarkTheme()
    val mapStyleOptions = remember(darkTheme) {
        if (darkTheme) {
            runCatching {
                MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
            }.getOrNull()
        } else {
            null
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(MANAUS_CENTER, DEFAULT_ZOOM)
    }

    // Permissão de localização: habilita o ponto azul "você está aqui" do Google Maps.
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result -> hasLocationPermission = result.values.any { it } }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    // Ao tocar num pin, centraliza o mapa no evento.
    LaunchedEffect(state.selectedEventId) {
        val id = state.selectedEventId ?: return@LaunchedEffect
        state.markers.firstOrNull { it.eventId == id }?.let { marker ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLng(LatLng(marker.latitude, marker.longitude)),
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SociableTheme.chip),
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapStyleOptions = mapStyleOptions,
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
            ),
        ) {
            visible.forEach { marker ->
                val isSelected = marker.eventId == selected?.eventId
                MarkerComposable(
                    marker.eventId,
                    marker.category,
                    isSelected,
                    state = rememberMarkerState(
                        key = marker.eventId,
                        position = LatLng(marker.latitude, marker.longitude),
                    ),
                    zIndex = if (isSelected) 1f else 0f,
                    onClick = {
                        viewModel.onMarkerSelected(marker.eventId)
                        true
                    },
                ) {
                    Pin(
                        id = pinRes(marker.category, isSecret = false),
                        size = if (isSelected) 52.dp else 40.dp,
                        contentDescription = "${marker.title} · ${marker.category.label}",
                    )
                }
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
                    focusedBorderColor = SociableTheme.ink,
                    unfocusedBorderColor = SociableTheme.ink,
                    focusedContainerColor = SociableTheme.surface,
                    unfocusedContainerColor = SociableTheme.surface,
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
                    color = SociableTheme.ink,
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
            MapButton("+", "Aumentar zoom") {
                scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) }
            }
            MapButton("−", "Diminuir zoom") {
                scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) }
            }
            MapButton("◎", "Centralizar no mapa de Manaus") {
                scope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(MANAUS_CENTER, DEFAULT_ZOOM),
                    )
                }
            }
        }

        // Cartão do evento selecionado
        StickerBox(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            fill = SociableTheme.surface,
            radius = 24.dp,
            contentPadding = PaddingValues(18.dp),
        ) {
            Column {
                if (selected != null) {
                    Text(
                        text = "${selected.category.label} · ${formatEventDay(selected.startAt)} · ${ManausTime.time(selected.startAt)}",
                        color = SociableTheme.muted,
                        fontSize = 12.sp,
                    )
                    Text(
                        text = selected.title,
                        color = SociableTheme.ink,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${formatPriceText(selected.isFree, selected.price)} · ${formatSpots(selected.goingCount, selected.capacity)}",
                        color = SociableTheme.ink,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = "📍 ${selected.venueName} · ${selected.address}",
                        color = SociableTheme.muted,
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
                        color = SociableTheme.ink,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Tente outro filtro ou outra busca.",
                        color = SociableTheme.muted,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun MapButton(text: String, description: String, onClick: () -> Unit) {
    StickerBox(
        modifier = Modifier.width(48.dp),
        fill = SociableTheme.surface,
        radius = 14.dp,
        shadow = 3.dp,
        height = 48.dp,
        contentAlignment = Alignment.Center,
        onClick = onClick,
    ) {
        Text(
            text = text,
            color = SociableTheme.ink,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.semantics { contentDescription = description },
        )
    }
}
