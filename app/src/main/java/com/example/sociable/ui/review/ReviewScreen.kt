package com.example.sociable.ui.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.core.time.ManausTime
import com.example.sociable.feature.review.ReviewViewModel
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.FormTextField
import com.example.sociable.ui.common.PrimaryButton
import com.example.sociable.ui.common.SectionTitle
import com.example.sociable.ui.common.SociableTopBar
import com.example.sociable.ui.common.formatAverage
import com.example.sociable.ui.common.message

private val StarColor = Color(0xFFF2B705)

@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = { SociableTopBar(title = "Avaliações", onBack = onBack) },
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.rating.hasReviews) {
                Text(
                    text = "★ ${formatAverage(state.rating.average)} · ${state.rating.count} avaliações",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                Text(
                    text = "Este evento ainda não tem avaliações.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (state.hasReviewed) {
                Text(
                    text = "Você já avaliou este evento. Obrigado pelo feedback!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                SectionTitle(text = "Deixe sua avaliação")

                Row {
                    (1..5).forEach { star ->
                        Text(
                            text = "★",
                            fontSize = 34.sp,
                            color = if (star <= state.selectedRating) {
                                StarColor
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            modifier = Modifier
                                .clickable { viewModel.onRatingChange(star) }
                                .padding(horizontal = 3.dp),
                        )
                    }
                }

                val ratingError = state.ratingError
                if (ratingError != null) {
                    Text(
                        text = ratingError.message(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                FormTextField(
                    value = state.comment,
                    onValueChange = viewModel::onCommentChange,
                    label = "Comentário (opcional)",
                    error = state.commentError,
                    singleLine = false,
                    minLines = 3,
                )

                ErrorText(error = state.error)

                if (state.isSubmitted) {
                    Text(
                        text = "Avaliação enviada!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                PrimaryButton(
                    text = "Enviar avaliação",
                    onClick = viewModel::onSubmitClick,
                    isLoading = state.isSubmitting,
                )
            }

            if (state.reviews.isNotEmpty()) {
                SectionTitle(
                    text = "O que as pessoas acharam",
                    modifier = Modifier.padding(top = 12.dp),
                )

                state.reviews.forEach { review ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = review.authorName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = "★".repeat(review.rating),
                                    color = StarColor,
                                )
                            }
                            if (review.comment.isNotBlank()) {
                                Text(
                                    text = review.comment,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                            Text(
                                text = ManausTime.date(review.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
