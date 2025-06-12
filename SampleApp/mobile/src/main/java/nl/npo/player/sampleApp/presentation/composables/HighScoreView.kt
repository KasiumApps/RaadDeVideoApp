package nl.npo.player.sampleApp.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nl.npo.hackathon.sampleApp.R
import nl.npo.player.sampleApp.shared.data.model.hackathon.HighScore

@Composable
fun HighScoreView(highScore: HighScore) {
    val tableData =
        highScore.scores.mapIndexed { index, score ->
            index to score
        }
    // Each cell of a column must have the same weight.
    val column1Weight = .7f // 30%
    val column2Weight = .3f // 70%
    // The LazyColumn will be our table. Notice the use of the weights below
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        // Here is the header
        item {
            Row(Modifier.background(Color(0xEEF56A00))) {
                TableCell(
                    text = stringResource(R.string.high_score_table_name),
                    weight = column1Weight,
                )
                TableCell(
                    text = stringResource(R.string.high_score_table_score),
                    weight = column2Weight,
                )
            }
        }
        // Here are all the lines of your table.
        items(
            tableData,
        ) {
            val (_, score) = it
            Row(Modifier.fillMaxWidth()) {
                TableCell(text = score.name, weight = column1Weight)
                TableCell(text = score.score.toString(), weight = column2Weight)
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
) {
    Text(
        text = text,
        Modifier
            .border(1.dp, Color.Black)
            .weight(weight)
            .padding(8.dp),
    )
}
