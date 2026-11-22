package com.example.sociable.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.data.model.CommunityDraft
import com.example.sociable.feature.community.CommunityListViewModel
import com.example.sociable.ui.common.BackLink
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.common.SociableTextField
import com.example.sociable.ui.common.StickerBox
import com.example.sociable.ui.theme.SociableTheme

@Composable
fun CommunityCreateScreen(
    viewModel: CommunityListViewModel,
    onCreate: (CommunityDraft) -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
) {
    val nameState = remember { mutableStateOf("") }
    val descriptionState = remember { mutableStateOf("") }
    val isPublicState = remember { mutableStateOf(true) }

    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SociableTheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        BackLink(onBack)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Criar comunidade",
            color = SociableTheme.ink,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))

        StickerBox(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            fill = SociableTheme.surface,
            radius = 20.dp,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        ) {
            Column {
                SociableTextField(
                    label = "Nome da comunidade",
                    value = nameState.value,
                    onChange = { nameState.value = it },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                SociableTextField(
                    label = "Descrição",
                    value = descriptionState.value,
                    onChange = { descriptionState.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    singleLine = false,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tipo de comunidade",
                    color = SociableTheme.ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SociableChip(
                        text = "Pública",
                        selected = isPublicState.value,
                        onClick = { isPublicState.value = true },
                    )
                    SociableChip(
                        text = "Privada",
                        selected = !isPublicState.value,
                        onClick = { isPublicState.value = false },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isCreating) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = SociableTheme.ink)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SociableButton(
                    text = "Cancelar",
                    onClick = onCancel,
                    secondary = true,
                )
                SociableButton(
                    text = "Criar",
                    onClick = {
                        onCreate(
                            CommunityDraft(
                                name = nameState.value,
                                description = descriptionState.value,
                                isPublic = isPublicState.value,
                            )
                        )
                    },
                    enabled = nameState.value.isNotBlank(),
                )
            }
        }
    }
}