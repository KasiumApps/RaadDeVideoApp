package nl.npo.player.sampleApp.shared.data.game

import nl.npo.player.sampleApp.shared.data.model.hackathon.AnswerOption
import nl.npo.player.sampleApp.shared.data.model.hackathon.AnswerQuestionResponse
import nl.npo.player.sampleApp.shared.data.model.hackathon.GameScore
import nl.npo.player.sampleApp.shared.data.model.hackathon.GameStartRequest
import nl.npo.player.sampleApp.shared.data.model.hackathon.GameStartResponse
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
        private val correctAnswerId = "id2"
        private var score = 0

        override suspend fun createGame(name: String): GameStartResponse =
            gameApi.startGame(gameStartRequest = GameStartRequest(name = name))

        override suspend fun getQuestion(
            gameId: String,
            questionId: String,
        ): Question =
            Question(
                questionId = "qId",
                prid = "AT_2160754",
                question = "Hoe heet de zanger(-es) om wie het draait in deze aflevering?",
                answerOptions =
                    listOf(
                        AnswerOption("id1", "FakeAnswer$score"),
                        AnswerOption(correctAnswerId, "Karsu"),
                        AnswerOption("id3", "FakeAnswer${score + 2}"),
                    ),
                firstSegmentId = "seg1",
            )

        override suspend fun getSegment(segmentId: String): Segment =
            Segment(segmentId = segmentId, startTime = 300, endTime = 310, nextSegmentId = "seg2")

        override suspend fun answerQuestion(
            questionId: String,
            answerId: String,
        ): AnswerQuestionResponse {
            val correct = answerId == correctAnswerId
            if (correct) score++
            return AnswerQuestionResponse(
                correct = correct,
                nextQuestionId = if (score < 2) "bla" else null,
            )
        }

        override suspend fun getScore(gameId: String): GameScore = GameScore(score = score)
    }
