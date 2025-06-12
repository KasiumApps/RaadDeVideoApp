package nl.npo.player.sampleApp.shared.data.game

import nl.npo.player.sampleApp.shared.data.model.hackathon.AnswerQuestionRequest
import nl.npo.player.sampleApp.shared.data.model.hackathon.AnswerQuestionResponse
import nl.npo.player.sampleApp.shared.data.model.hackathon.GameScore
import nl.npo.player.sampleApp.shared.data.model.hackathon.GameStartRequest
import nl.npo.player.sampleApp.shared.data.model.hackathon.GameStartResponse
import nl.npo.player.sampleApp.shared.data.model.hackathon.HighScore
import nl.npo.player.sampleApp.shared.data.model.hackathon.Question
import nl.npo.player.sampleApp.shared.data.model.hackathon.Segment
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

internal interface GameApi {
    @Headers(
        "Accept: */*",
        "Content-Type: application/json",
        "Accept-Encoding: gzip, deflate, br",
    )
    @POST("/game/start")
    suspend fun startGame(
        @Body gameStartRequest: GameStartRequest,
    ): GameStartResponse

    @Headers(
        "Accept: */*",
        "Accept-Encoding: gzip, deflate, br",
    )
    @GET("/game/question")
    suspend fun getQuestion(
        @Query("game") gameId: String,
        @Query("question") questionId: String,
    ): Question

    @Headers(
        "Accept: */*",
        "Accept-Encoding: gzip, deflate, br",
    )
    @GET("/game/question/segment")
    suspend fun getSegment(
        @Query("game") gameId: String,
        @Query("question") questionId: String,
        @Query("segment") segmentId: String,
    ): Segment

    @Headers(
        "Accept: */*",
        "Content-Type: application/json",
        "Accept-Encoding: gzip, deflate, br",
    )
    @POST("/game/question")
    suspend fun sendAnswer(
        @Query("game") gameId: String,
        @Query("question") questionId: String,
        @Body answerBody: AnswerQuestionRequest,
    ): AnswerQuestionResponse

    @Headers(
        "Accept: */*",
        "Accept-Encoding: gzip, deflate, br",
    )
    @GET("/game/score")
    suspend fun getScore(
        @Query("game") gameId: String,
    ): GameScore

    @Headers(
        "Accept: */*",
        "Accept-Encoding: gzip, deflate, br",
    )
    @GET("/game/high-score")
    suspend fun getHighScore(): HighScore
}
