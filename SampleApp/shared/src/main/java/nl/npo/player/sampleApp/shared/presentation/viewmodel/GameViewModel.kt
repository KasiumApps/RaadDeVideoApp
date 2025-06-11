package nl.npo.player.sampleApp.shared.presentation.viewmodel

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
            updateGameState { GameState() }
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
                    val startResponse = gameRepository.createGame(name)
                    updateGameState {
                        GameState(
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
                    updateGameState {
                        copy(
                            progressState = ProgressState.Loading,
                        )
                    }
                    val answerQuestionResponse =
                        gameRepository.answerQuestion(question.questionId, answerId = answerOption.id)
                    updateScore()
                    updateGameState {
                        copy(progressState = ProgressState.AnswerResult(answerQuestionResponse.correct))
                    }
                    delay(1500)

                    if (answerQuestionResponse.nextQuestionId == null) {
                        updateGameState {
                            copy(progressState = ProgressState.GameEnded)
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
            getSegment(segmentId = segment.nextSegmentId)
            updateGameState {
                copy(progressState = ProgressState.AnswerQuestion)
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
                val startResponse =
                    currentState.gameStartResponse
                        ?: throw IllegalStateException("No start response when trying to retrieve question")

                tryCatch {
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

                    var sourceConfig = fetchAndMergeSource(question.prid)
                    getSegment(question.firstSegmentId)
                    sourceConfig = sourceConfig.copy(overrideSegment = _gameState.value.segment?.toSegment())
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

        private suspend fun fetchAndMergeSource(prid: String): NPOSourceConfig {
            val token = getToken(prid)
            return NPOPlayerLibrary.StreamLink.getNPOSourceConfig(JWTString(token))
        }

        private suspend fun getToken(prid: String): String =
            when (val tokenResult = tokenProvider.createToken(prid, true)) {
                is StreamInfoResult.Success -> tokenResult.data.token
                else -> {
                    throw IllegalArgumentException("Could not retrieve token for PRID: $prid")
                }
            }

        private fun getSegment(segmentId: String) {
            viewModelScope.launch {
                updateGameState {
                    copy(
                        progressState = ProgressState.Loading,
                    )
                }
                tryCatch {
                    val segment = gameRepository.getSegment(segmentId)
                    updateGameState {
                        copy(segment = segment)
                    }
                }
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
    }
