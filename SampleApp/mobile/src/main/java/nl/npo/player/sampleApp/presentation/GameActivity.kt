package nl.npo.player.sampleApp.presentation

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.selectAll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.hackathon.sampleApp.R
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.attachToLifecycle
import nl.npo.player.library.domain.analytics.model.PageConfiguration
import nl.npo.player.library.domain.exception.NPOPlayerException
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.library.presentation.compose.PlayerSurface
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.sampleApp.presentation.composables.HighScoreView
import nl.npo.player.sampleApp.shared.data.model.hackathon.AnswerOption
import nl.npo.player.sampleApp.shared.data.model.hackathon.HighScore
import nl.npo.player.sampleApp.shared.data.model.hackathon.Question
import nl.npo.player.sampleApp.shared.presentation.game.GameState
import nl.npo.player.sampleApp.shared.presentation.game.ProgressState
import nl.npo.player.sampleApp.shared.presentation.viewmodel.GameViewModel
import nl.npo.player.sampleApp.shared.presentation.viewmodel.LibrarySetupViewModel
import nl.npo.player.sampleApp.shared.presentation.viewmodel.PlayerViewModel

@AndroidEntryPoint
class GameActivity : BaseActivity() {
    private val libraryViewModel by viewModels<LibrarySetupViewModel>()
    private val gameModel by viewModels<GameViewModel>()
    private var player: NPOPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLibraryInitialization()
        logPageAnalytics("GameActivity")
        setContent {
            val gameState by gameModel.gameState.collectAsState()
            MaterialTheme {
                GameView(gameState)
            }
        }
    }

    private fun initPlayer() {
        try {
            player =
                NPOPlayerLibrary
                    .getPlayer(
                        context = this,
                        npoPlayerConfig = NPOPlayerConfig(),
                        pageTracker =
                            pageTracker?.let { PlayerTagProvider.getPageTracker(it) }
                                ?: PlayerTagProvider.getPageTracker(
                                    PageConfiguration("Quiz"),
                                ),
                    ).apply {
                        isAutoPlayEnabled = true
                        remoteControlMediaInfoCallback = PlayerViewModel.remoteCallback
                        attachToLifecycle(lifecycle)
                    }
        } catch (e: NPOPlayerException.PlayerInitializationException) {
            AlertDialog
                .Builder(this)
                .setTitle("Error")
                .setMessage("Player Analytics not initialized correctly. ${e.message}")
                .setCancelable(false)
                .setPositiveButton(
                    "Ok",
                ) { _, _ -> finish() }
                .show()
            return
        }
    }

    @Composable
    fun GameView(gameState: GameState) {
        LaunchedEffect(gameState.npoSourceConfig) {
            Log.d("SampleAppTest", "Source config updated: ${gameState.npoSourceConfig}")
            Log.d("SampleAppTest", "Source config segment: ${gameState.npoSourceConfig?.segment}")
            val currentSource = gameState.npoSourceConfig
            if (currentSource != null) {
                player?.let {
                    gameModel.loadStream(npoPlayer = it, currentSource)
                    Log.d("SampleAppTest", "Stream should be loaded!")
                }
            } else {
                player?.unload()
            }
        }
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (gameState.name != null) {
                GameHeader(name = gameState.name, score = gameState.score.score)
                HorizontalDivider(thickness = 2.dp)
            }

            when (val progressState = gameState.progressState) {
                ProgressState.AnswerQuestion -> {
                    val question = gameState.question
                    val segment = gameState.segment
                    val npoPlayer = player
                    if (question != null && segment != null && npoPlayer != null) {
                        QuestionView(
                            npoPlayer = npoPlayer,
                            question = question,
                            onAnswer = { answerOption ->
                                gameModel.answerQuestion(
                                    question = question,
                                    answerOption = answerOption,
                                )
                            },
                            hintAvailable = segment.hasMoreSegments,
                            onHint = {
                                gameModel.getNextSegment(segment)
                            },
                        )
                    } else {
                        // TODO throw error
                    }
                }

                is ProgressState.Error -> {
                    ErrorView(
                        errorState = progressState,
                    ) { gameModel.resetGame() }
                }

                ProgressState.GameEnded -> {
                    EndView(
                        player = player,
                        npoSourceConfig = gameState.npoSourceConfig,
                        name = gameState.name ?: "",
                        score = gameState.score.score,
                        highScore = gameState.highScore,
                    ) {
                        gameModel.resetGame()
                    }
                }

                ProgressState.Init -> {
                    StartGameView(
                        previousName = gameState.name,
                        startGame = { name ->
                            gameModel.startGame(name)
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                ProgressState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                    )
                }

                is ProgressState.AnswerResult -> AnswerResultView(correct = progressState.correct)
            }
        }
    }

    @Composable
    fun AnswerResultView(correct: Boolean) {
        val text =
            stringResource(if (correct) R.string.result_correct else R.string.result_incorrect)
        val color = if (correct) Color.Green else Color.Red
        Text(text = text, color = color)
    }

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

        val onButtonClick = {
            val name = editTextState.text.toString()
            if (name.isBlank()) {
                Toast
                    .makeText(
                        this@GameActivity,
                        "Name should not be empty",
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

    private fun checkLibraryInitialization() {
        libraryViewModel.setupLibrary(withNPOTag = true) {
            initPlayer()
        }
    }
}
