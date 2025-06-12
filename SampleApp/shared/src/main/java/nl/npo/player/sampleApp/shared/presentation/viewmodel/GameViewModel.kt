package nl.npo.player.sampleApp.shared.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.npo.player.library.NPOPlayerLibrary
import nl.npo.player.library.data.extensions.copy
import nl.npo.player.library.domain.common.model.JWTString
import nl.npo.player.library.domain.player.NPOPlayer
import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.sampleApp.shared.data.model.hackathon.AnswerOption
import nl.npo.player.sampleApp.shared.data.model.hackathon.Question
import nl.npo.player.sampleApp.shared.data.model.hackathon.Segment
import nl.npo.player.sampleApp.shared.data.model.hackathon.toSegment
import nl.npo.player.sampleApp.shared.domain.TokenProvider
import nl.npo.player.sampleApp.shared.domain.hackathon.GameRepository
import nl.npo.player.sampleApp.shared.domain.model.StreamInfoResult
import nl.npo.player.sampleApp.shared.presentation.game.GameState
import nl.npo.player.sampleApp.shared.presentation.game.ProgressState
import javax.inject.Inject

@HiltViewModel
class GameViewModel
    @Inject
    constructor(
        private val gameRepository: GameRepository,
        private val tokenProvider: TokenProvider,
    ) : ViewModel() {
        private val _gameState = MutableStateFlow(GameState())
        val gameState: StateFlow<GameState> = _gameState.asStateFlow()

        fun resetGame() {
            updateGameState { GameState(name = name) }
        }

        fun loadStream(
            npoPlayer: NPOPlayer,
            npoSourceConfig: NPOSourceConfig,
        ) {
            viewModelScope.launch {
                tryCatch {
                    npoPlayer.loadStream(npoSourceConfig)
                }
            }
        }

        fun startGame(name: String) {
            viewModelScope.launch {
                tryCatch {
                    updateGameState {
                        GameState(progressState = ProgressState.Loading)
                    }
                    val startResponse = gameRepository.createGame(name)
                    updateGameState {
                        copy(
                            name = name,
                            progressState = ProgressState.Loading,
                            gameStartResponse = startResponse,
                        )
                    }
                    nextStep()
                }
            }
        }

        fun answerQuestion(
            question: Question,
            answerOption: AnswerOption,
        ) {
            viewModelScope.launch {
                tryCatch {
                    val gameId =
                        _gameState.value.gameStartResponse?.gameId
                            ?: throw IllegalStateException("No start response when trying to retrieve question")
                    updateGameState {
                        copy(
                            progressState = ProgressState.Loading,
                            npoSourceConfig = null,
                        )
                    }
                    val answerQuestionResponse =
                        gameRepository.answerQuestion(
                            gameId = gameId,
                            questionId = question.questionId,
                            answerId = answerOption.id,
                        )
                    updateScore()
                    updateGameState {
                        copy(progressState = ProgressState.AnswerResult(answerQuestionResponse.correct))
                    }
                    delay(1500)

                    if (answerQuestionResponse.nextQuestionId == null) {
                        val highScore = gameRepository.getHighScore()
                        updateGameState {
                            copy(progressState = ProgressState.GameEnded, npoSourceConfig = endConfig, highScore = highScore)
                        }
                    } else {
                        updateGameState {
                            copy(
                                progressState = ProgressState.Loading,
                            )
                        }
                        getQuestion(answerQuestionResponse.nextQuestionId)
                    }
                }
            }
        }

        fun getNextSegment(segment: Segment) {
            viewModelScope.launch {
                val currentState = _gameState.value
                tryCatch {
                    val questionId =
                        currentState.question?.questionId
                            ?: throw IllegalStateException("No question when trying to retrieve segment")
                    val nextSegmentId =
                        segment.nextSegmentId
                            ?: throw IllegalStateException("No question when trying to retrieve segment")
                    val gameId =
                        currentState.gameStartResponse?.gameId
                            ?: throw IllegalStateException("No start response when trying to retrieve segment")
                    val currentSourceConfig =
                        currentState.npoSourceConfig
                            ?: throw IllegalStateException("No source config when trying to retrieve new segment/hint")
                    updateGameState {
                        copy(progressState = ProgressState.Loading)
                    }
                    val newSegment = getSegment(gameId, questionId, segmentId = nextSegmentId)

                    Log.d("SampleAppTest", "GetNextSegment new segment: $newSegment")
                    updateGameState {
                        copy(
                            progressState = ProgressState.AnswerQuestion,
                            segment = newSegment,
                            npoSourceConfig = currentSourceConfig.copy(overrideSegment = newSegment?.toSegment()),
                        )
                    }
                }
            }
        }

        private fun nextStep() {
            val currentState = _gameState.value
            if (currentState.question == null || currentState.progressState == ProgressState.Loading) {
                getQuestion(currentState.question?.questionId)
            }
        }

        private fun getQuestion(questionId: String?) {
            viewModelScope.launch {
                val currentState = _gameState.value

                tryCatch {
                    val startResponse =
                        currentState.gameStartResponse
                            ?: throw IllegalStateException("No start response when trying to retrieve question")
                    updateGameState {
                        copy(
                            progressState = ProgressState.Loading,
                        )
                    }
                    val question =
                        gameRepository.getQuestion(
                            startResponse.gameId,
                            questionId
                                ?: startResponse.startQuestionId,
                        )

                    val segment = getSegment(startResponse.gameId, question.questionId, question.firstSegmentId)

                    val sourceConfig = fetchAndMergeSource(question.prid, segment)
                    updateGameState {
                        copy(
                            question = question,
                            progressState = ProgressState.AnswerQuestion,
                            npoSourceConfig = sourceConfig,
                        )
                    }
                }
            }
        }

        private suspend fun fetchAndMergeSource(
            prid: String,
            segment: Segment?,
        ): NPOSourceConfig {
            val token = getToken(prid)
            return NPOPlayerLibrary.StreamLink
                .getNPOSourceConfig(JWTString(token))
                .copy(overrideAutoPlay = true, overrideSegment = segment?.toSegment())
        }

        private suspend fun getToken(prid: String): String =
            when (val tokenResult = tokenProvider.createToken(prid, true)) {
                is StreamInfoResult.Success -> tokenResult.data.token
                else -> {
                    throw IllegalArgumentException("Could not retrieve token for PRID: $prid")
                }
            }

        private suspend fun getSegment(
            gameId: String,
            questionId: String,
            segmentId: String,
        ): Segment? {
            updateGameState {
                copy(
                    progressState = ProgressState.Loading,
                )
            }
            return tryCatch {
                val segment = gameRepository.getSegment(gameId, questionId, segmentId)
                updateGameState {
                    copy(segment = segment)
                }
                return@tryCatch segment
            }
        }

        private fun updateScore() {
            viewModelScope.launch {
                val currentState = _gameState.value
                tryCatch {
                    val startResponse =
                        currentState.gameStartResponse
                            ?: throw IllegalStateException("No start response when trying to retrieve score")
                    val score = gameRepository.getScore(startResponse.gameId)
                    updateGameState {
                        copy(score = score)
                    }
                }
            }
        }

        private suspend fun <T> tryCatch(
            debugLogging: Boolean = true,
            block: suspend () -> T,
        ) = try {
            block()
        } catch (e: Throwable) {
            if (debugLogging) println(e.message)
            updateGameState {
                copy(progressState = ProgressState.Error(e.message))
            }
            null
        }

        private fun updateGameState(block: GameState.() -> GameState) {
            val currentState = _gameState.value
            _gameState.value = currentState.block()
        }

        companion object {
            private val endConfig =
                object : NPOSourceConfig {
                    override val streamUrl = "https://npo.nl/videos/NPO_De%2BPlek_Flight%2B2_General_90s_Online_16x9.mp4"
                    override val uniqueId = "NPO_De%2BPlek_Flight%2B2_General_90s_Online"
                    override val autoPlay = true
                    override val segment =
                        nl.npo.player.library.domain.streamLink.model
                            .Segment("id", 1, 89)
                }
        }
    }
