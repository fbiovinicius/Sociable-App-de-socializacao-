package com.example.sociable.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sociable.core.time.ManausTime
import com.example.sociable.feature.review.ReviewViewModel
import com.example.sociable.ui.common.BackLink
import com.example.sociable.ui.common.ErrorText
import com.example.sociable.ui.common.FormTextField
import com.example.sociable.ui.common.ScreenTitle
import com.example.sociable.ui.common.SociableButton
import com.example.sociable.ui.common.StickerBox
import com.example.sociable.ui.common.formatAverage
import com.example.sociable.ui.common.message
import com.example.sociable.ui.theme.Coral
import com.example.sociable.ui.theme.Plum
import com.example.sociable.ui.theme.Rose
import com.example.sociable.ui.theme.SCream
import com.example.sociable.ui.theme.SInk
import com.example.sociable.ui.theme.Star

@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SCream)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 28.dp, bottom = 24.dp),
    ) {
        BackLink(onBack)
        Spacer(modifier = Modifier.height(8.dp))
        ScreenTitle("Avaliações", sub = "O que a galera achou")

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = SInk)
            }
            return@Column
        }

        StickerBox(
            modifier = Modifier.fillMaxWidth(),
            fill = Rose,
            radius = 18.dp,
            contentPadding = PaddingValues(14.dp),
        ) {
            if (state.rating.hasReviews) {
                Text(
                    text = "★ ${formatAverage(state.rating.average)} · ${state.rating.count} avaliações",
                    color = SInk,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                Text(
                    text = "Este evento ainda não tem avaliações.",
                    color = Plum,
                    fontSize = 14.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (state.hasReviewed) {
            Text(
                text = "Você já avaliou este evento. Obrigado pelo feedback!",
                color = SInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        } else {
            Text(
                text = "Deixe sua avaliação",
                color = SInk,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row {
                (1..5).forEach { star ->
                    Text(
                        text = "★",
                        fontSize = 34.sp,
                        color = if (star <= state.selectedRating) Star else Plum.copy(alpha = 0.35f),
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
                    color = Coral,
                    fontSize = 12.sp,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
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
                    color = SInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            SociableButton(
                text = "Enviar avaliação",
                onClick = viewModel::onSubmitClick,
                isLoading = state.isSubmitting,
            )
        }

        if (state.reviews.isNotEmpty()) {
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "O que as pessoas acharam",
                color = SInk,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.reviews.forEach { review ->
                    StickerBox(
                        modifier = Modifier.fillMaxWidth(),
                        fill = SCream,
                        radius = 18.dp,
                        contentPadding = PaddingValues(14.dp),
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = review.authorName,
                                    color = SInk,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "★".repeat(review.rating),
                                    color = Star,
                                    fontSize = 14.sp,
                                )
                            }
                            if (review.comment.isNotBlank()) {
                                Text(
                                    text = review.comment,
                                    color = SInk,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                            Text(
                                text = ManausTime.date(review.createdAt),
                                color = Plum,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
