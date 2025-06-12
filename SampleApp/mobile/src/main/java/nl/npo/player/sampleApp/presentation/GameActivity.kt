package nl.npo.player.sampleApp.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.attachToLifecycle
import nl.npo.player.library.domain.analytics.model.PageConfiguration
import nl.npo.player.library.domain.exception.NPOPlayerException
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.npotag.PlayerTagProvider
import nl.npo.player.library.presentation.model.NPOPlayerConfig
import nl.npo.player.sampleApp.presentation.composables.AnswerResultView
import nl.npo.player.sampleApp.presentation.composables.EndView
import nl.npo.player.sampleApp.presentation.composables.ErrorView
import nl.npo.player.sampleApp.presentation.composables.GameHeader
import nl.npo.player.sampleApp.presentation.composables.QuestionView
import nl.npo.player.sampleApp.presentation.composables.StartGameView
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

    override fun onResume() {
        super.onResume()
        if (gameModel.gameState.value.npoSourceConfig != null) {
            player?.play()
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

    private fun checkLibraryInitialization() {
        libraryViewModel.setupLibrary(withNPOTag = true) {
            initPlayer()
        }
    }
}
