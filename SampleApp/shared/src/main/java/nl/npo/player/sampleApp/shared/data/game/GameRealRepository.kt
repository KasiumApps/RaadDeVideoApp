package nl.npo.player.sampleApp.shared.data.game

import nl.npo.player.sampleApp.shared.data.model.hackathon.AnswerQuestionRequest
import nl.npo.player.sampleApp.shared.data.model.hackathon.AnswerQuestionResponse
import nl.npo.player.sampleApp.shared.data.model.hackathon.GameScore
import nl.npo.player.sampleApp.shared.data.model.hackathon.GameStartRequest
import nl.npo.player.sampleApp.shared.data.model.hackathon.GameStartResponse
import nl.npo.player.sampleApp.shared.data.model.hackathon.HighScore
import nl.npo.player.sampleApp.shared.data.model.hackathon.Question
import nl.npo.player.sampleApp.shared.data.model.hackathon.Segment
import nl.npo.player.sampleApp.shared.domain.hackathon.GameRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class GameRealRepository
    @Inject
    constructor(
        val gameApi: GameApi,
    ) : GameRepository {
        override suspend fun createGame(name: String): GameStartResponse =
            gameApi.startGame(gameStartRequest = GameStartRequest(name = name))

        override suspend fun getQuestion(
            gameId: String,
            questionId: String,
        ): Question = gameApi.getQuestion(gameId = gameId, questionId = questionId)

        override suspend fun getSegment(
            gameId: String,
            questionId: String,
            segmentId: String,
        ): Segment = gameApi.getSegment(gameId = gameId, questionId = questionId, segmentId = segmentId)

        override suspend fun answerQuestion(
            gameId: String,
            questionId: String,
            answerId: String,
        ): AnswerQuestionResponse =
            gameApi.sendAnswer(
                gameId = gameId,
                questionId = questionId,
                answerBody = AnswerQuestionRequest(questionId, answerId),
            )

        override suspend fun getScore(gameId: String): GameScore = gameApi.getScore(gameId = gameId)

        override suspend fun getHighScore(): HighScore = gameApi.getHighScore()
    }
