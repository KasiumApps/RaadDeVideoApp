package nl.npo.player.sampleApp.shared.data.game

import nl.npo.player.sampleApp.shared.data.model.hackathon.GameStartRequest
import nl.npo.player.sampleApp.shared.data.model.hackathon.GameStartResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

internal interface GameApi {
    @Headers(
        "Accept: */*",
        "Content-Type: application/json",
        "Accept-Encoding: gzip, deflate, br",
    )
    @POST("/game/start")
    suspend fun startGame(
        @Header("x-api-key") apiKey: String = "e361a055-fac0-44a5-ab40-eb29791df1d3",
        @Body gameStartRequest: GameStartRequest,
    ): GameStartResponse
}
