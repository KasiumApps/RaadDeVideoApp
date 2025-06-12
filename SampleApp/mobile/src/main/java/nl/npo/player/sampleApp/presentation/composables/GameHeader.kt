package nl.npo.player.sampleApp.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import nl.npo.hackathon.sampleApp.R

@Composable
fun GameHeader(
    name: String?,
    score: Int,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        if (name != null) {
            Text(stringResource(R.string.player_name, name))

            Text(stringResource(R.string.player_score, score))
        }
    }
}
