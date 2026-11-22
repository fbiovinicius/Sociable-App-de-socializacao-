package com.example.sociable.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sociable.data.model.ReportReason
import com.example.sociable.ui.common.SociableChip
import com.example.sociable.ui.common.SociableTextField
import com.example.sociable.ui.theme.SociableTheme

/**
 * Diálogo de denúncia de um evento.
 *
 * A pessoa escolhe o motivo (obrigatório) e pode escrever um comentário.
 * O envio é feito pelo ViewModel; aqui é só a entrada de dados.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportEventDialog(
    eventTitle: String,
    isSending: Boolean,
    errorMessage: String?,
    onConfirm: (ReportReason, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var reason by remember { mutableStateOf<ReportReason?>(null) }
    var comment by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { if (!isSending) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SociableTheme.surface,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Denunciar evento",
                    color = SociableTheme.ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "\"$eventTitle\"",
                    color = SociableTheme.muted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                )

                Text(
                    text = "O que está errado?",
                    color = SociableTheme.ink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ReportReason.entries.forEach { option ->
                        SociableChip(
                            text = option.label,
                            selected = reason == option,
                            onClick = { reason = option },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                SociableTextField(
                    label = "Comentário (opcional)",
                    value = comment,
                    onChange = { comment = it.take(500) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = SociableTheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss, enabled = !isSending) {
                        Text("Cancelar")
                    }
                    TextButton(
                        onClick = { reason?.let { onConfirm(it, comment) } },
                        enabled = reason != null && !isSending,
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(18.dp),
                                color = SociableTheme.ink,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Enviar denúncia", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
