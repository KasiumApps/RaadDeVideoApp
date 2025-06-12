package nl.npo.player.sampleApp.presentation.composables

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.selectAll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import nl.npo.hackathon.sampleApp.R

@Composable
fun StartGameView(
    previousName: String?,
    startGame: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val editTextState = rememberTextFieldState()
    LaunchedEffect(previousName) {
        previousName?.let {
            editTextState.edit {
                replace(0, this.length, it)
                selectAll()
            }
        }
    }
    val context = LocalContext.current
    val nameBlankError = stringResource(R.string.name_not_entered)

    val onButtonClick = {
        val name = editTextState.text.toString()
        if (name.isBlank()) {
            Toast
                .makeText(
                    context,
                    nameBlankError,
                    Toast.LENGTH_LONG,
                ).show()
        } else {
            startGame.invoke(name)
        }
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround,
    ) {
        Column {
            Text(
                text = stringResource(R.string.welcome_text),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                state = editTextState,
                label = { Text(text = stringResource(R.string.name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                lineLimits = TextFieldLineLimits.SingleLine,
                onKeyboardAction = {
                    onButtonClick.invoke()
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
        }
        Button(
            onClick = onButtonClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.start_btn))
        }
    }
}
