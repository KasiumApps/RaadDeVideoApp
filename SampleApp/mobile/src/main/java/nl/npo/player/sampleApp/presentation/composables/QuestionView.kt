package nl.npo.player.sampleApp.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import nl.npo.hackathon.sampleApp.R
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.presentation.compose.PlayerSurface
import nl.npo.player.sampleApp.shared.data.model.hackathon.AnswerOption
import nl.npo.player.sampleApp.shared.data.model.hackathon.Question

@Composable
fun QuestionView(
    npoPlayer: NPOPlayer,
    question: Question,
    onAnswer: (AnswerOption) -> Unit,
    hintAvailable: Boolean,
    onHint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround,
    ) {
        Text(
            text = question.question,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
        )

        PlayerSurface(
            player = npoPlayer,
            canShowAds = false,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
        )

        FlowRow(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth(),
        ) {
            question.answerOptions.forEach { answer ->
                Button(
                    onClick = {
                        onAnswer.invoke(answer)
                    },
                ) {
                    Text(answer.text)
                }
            }
        }
        if (hintAvailable) {
            Button(
                onClick = onHint,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.segment_hint)) }
        }
    }
}
