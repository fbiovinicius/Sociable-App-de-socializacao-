package com.example.sociable.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.sociable.R
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.common.StickerBox
import com.example.sociable.ui.map.DEFAULT_ZOOM
import com.example.sociable.ui.map.MANAUS_CENTER
import com.example.sociable.ui.map.USER_ZOOM
import com.example.sociable.ui.map.currentLatLngOrNull
import com.example.sociable.ui.map.hasLocationPermission
import com.example.sociable.ui.theme.SociableTheme
import com.example.sociable.ui.theme.isSociableDarkTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Mapa em tela cheia para escolher o local do evento com um toque.
 *
 * [initial] é o ponto já escolhido antes (se houver). Sem ele, o mapa abre na
 * posição do aparelho, quando há permissão, ou em Manaus.
 */
@Composable
fun LocationPickerDialog(
    initial: LatLng?,
    onConfirm: (LatLng) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var picked by remember { mutableStateOf(initial) }
    val hasPermission = remember { context.hasLocationPermission() }

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
        position = if (initial != null) {
            CameraPosition.fromLatLngZoom(initial, USER_ZOOM)
        } else {
            CameraPosition.fromLatLngZoom(MANAUS_CENTER, DEFAULT_ZOOM)
        }
    }

    // Sem ponto anterior, tenta abrir perto de quem está criando o evento.
    LaunchedEffect(Unit) {
        if (initial != null) return@LaunchedEffect
        context.currentLatLngOrNull()?.let { position ->
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(position, USER_ZOOM))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SociableTheme.background),
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasPermission,
                    mapStyleOptions = mapStyleOptions,
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false,
                ),
                onMapClick = { picked = it },
            ) {
                picked?.let { point ->
                    val markerState = remember(point) { MarkerState(position = point) }
                    Marker(state = markerState, title = "Local do evento")
                }
            }

            SociableChip(
                text = "✕ Cancelar",
                selected = false,
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(16.dp),
            )

            StickerBox(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                fill = SociableTheme.surface,
                radius = 24.dp,
                contentPadding = PaddingValues(18.dp),
            ) {
                Column {
                    Text(
                        text = if (picked == null) {
                            "Toque no mapa para marcar o local"
                        } else {
                            "Local marcado. Toque de novo para mudar."
                        },
                        color = SociableTheme.ink,
                        fontSize = 14.sp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SociableButton(
                        text = "Confirmar local",
                        enabled = picked != null,
                        onClick = { picked?.let(onConfirm) },
                    )
                }
            }
        }
    }
}
