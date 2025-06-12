package nl.npo.player.sampleApp.shared.data.model.hackathon

data class HighScore(
    val scores: List<Score>,
)

data class Score(
    val name: String,
    val score: Int,
)
