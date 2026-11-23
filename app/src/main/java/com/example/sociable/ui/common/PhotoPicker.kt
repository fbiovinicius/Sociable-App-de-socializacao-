package com.example.sociable.ui.common

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.sociable.ui.theme.SociableTheme

/**
 * Devolve uma função que abre a galeria do celular (seletor de fotos do Android, sem
 * permissão de armazenamento) e chama [onPicked] com o `content://` escolhido.
 */
@Composable
fun rememberPhotoPicker(onPicked: (String) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onPicked(uri.toString())
    }
    return {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

/** Capa do evento: área "Adicionar imagem" ou a foto escolhida com Trocar / Remover. */
@Composable
fun EventCoverPicker(
    imageUrl: String,
    isUploading: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (imageUrl.isBlank()) {
            Box(contentAlignment = Alignment.Center) {
                AddImageBox(
                    title = if (isUploading) "Enviando foto..." else "Adicionar imagem do evento",
                    subtitle = "Escolha uma foto da galeria do celular",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp),
                    onClick = if (isUploading) null else onPick,
                )
            }
        } else {
            val shape = RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
                    .clip(shape)
                    .border(2.dp, SociableTheme.ink, shape),
                contentAlignment = Alignment.Center,
            ) {
                RemoteImage(
                    url = imageUrl,
                    contentDescription = "Capa do evento",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp),
                )
                if (isUploading) CircularProgressIndicator(color = SociableTheme.accent)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SociableButton(
                    text = "Trocar foto",
                    onClick = onPick,
                    secondary = true,
                    enabled = !isUploading,
                    modifier = Modifier.weight(1f),
                )
                SociableButton(
                    text = "Remover",
                    onClick = onRemove,
                    secondary = true,
                    enabled = !isUploading,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
