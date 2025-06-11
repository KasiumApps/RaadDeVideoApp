package nl.npo.player.sampleApp.shared.data.model.hackathon

data class AnswerQuestionResponse(
    val correct: Boolean,
    val nextQuestionId: String?,
)
