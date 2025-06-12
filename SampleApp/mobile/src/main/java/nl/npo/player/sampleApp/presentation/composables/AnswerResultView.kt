package nl.npo.player.sampleApp.presentation.composables

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import nl.npo.hackathon.sampleApp.R

@Composable
fun AnswerResultView(correct: Boolean) {
    val text =
        stringResource(if (correct) R.string.result_correct else R.string.result_incorrect)
    val color = if (correct) Color.Green else Color.Red
    Text(text = text, color = color, fontWeight = FontWeight.Bold, fontSize = 20.sp)
}
