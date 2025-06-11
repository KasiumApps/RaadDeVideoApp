package nl.npo.player.sampleApp.shared.presentation.game

import nl.npo.player.library.domain.player.model.NPOSourceConfig
import nl.npo.player.sampleApp.shared.data.model.hackathon.GameScore
import nl.npo.player.sampleApp.shared.data.model.hackathon.GameStartResponse
import nl.npo.player.sampleApp.shared.data.model.hackathon.Question
import nl.npo.player.sampleApp.shared.data.model.hackathon.Segment

data class GameState(
    val progressState: ProgressState = ProgressState.Init,
    val name: String? = null,
    val gameStartResponse: GameStartResponse? = null,
    val question: Question? = null,
    val segment: Segment? = null,
    val npoSourceConfig: NPOSourceConfig? = null,
    val score: GameScore = GameScore(),
)
