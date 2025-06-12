package nl.npo.player.sampleApp.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nl.npo.hackathon.sampleApp.R
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.presentation.compose.PlayerSurface
import nl.npo.player.sampleApp.shared.data.model.hackathon.HighScore

@Composable
fun EndView(
    player: NPOPlayer?,
    npoSourceConfig: NPOSourceConfig?,
    name: String,
    score: Int,
    highScore: HighScore?,
    restart: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (npoSourceConfig != null) {
            player?.let {
                PlayerSurface(
                    player = it,
                    canShowAds = false,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                )
            }
        }
        Text(stringResource(R.string.end_text, name, score))
        Button(modifier = Modifier.align(Alignment.CenterHorizontally), onClick = {
            restart.invoke()
        }) {
            Text(
                stringResource(R.string.restart_btn),
            )
        }
        highScore?.let { highScores ->
            HighScoreView(highScores)
        }
    }
}
