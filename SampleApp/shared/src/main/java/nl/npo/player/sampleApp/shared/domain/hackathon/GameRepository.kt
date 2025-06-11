package nl.npo.player.sampleApp.shared.domain.hackathon

import nl.npo.player.sampleApp.shared.data.model.hackathon.AnswerQuestionResponse
import nl.npo.player.sampleApp.shared.data.model.hackathon.GameScore
import nl.npo.player.sampleApp.shared.data.model.hackathon.GameStartResponse
import nl.npo.player.sampleApp.shared.data.model.hackathon.Question
import nl.npo.player.sampleApp.shared.data.model.hackathon.Segment

interface GameRepository {
    suspend fun createGame(name: String): GameStartResponse

    suspend fun getQuestion(
        gameId: String,
        questionId: String,
    ): Question

    suspend fun getSegment(segmentId: String): Segment

    suspend fun answerQuestion(
        questionId: String,
        answerId: String,
    ): AnswerQuestionResponse

    suspend fun getScore(gameId: String): GameScore
}
