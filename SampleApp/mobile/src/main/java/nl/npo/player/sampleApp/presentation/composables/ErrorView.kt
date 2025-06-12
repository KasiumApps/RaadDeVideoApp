package nl.npo.player.sampleApp.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nl.npo.hackathon.sampleApp.R
import nl.npo.player.sampleApp.shared.presentation.game.ProgressState


@Composable
fun ErrorView(
    errorState: ProgressState.Error,
    restart: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        Text(
            text = errorState.errorMessage ?: "",
        )
        Button(modifier = Modifier.align(Alignment.CenterHorizontally), onClick = {
            restart.invoke()
        }) {
            Text(
                stringResource(R.string.restart_btn),
            )
        }
    }
}
