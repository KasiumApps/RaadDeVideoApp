package nl.npo.player.sampleApp.shared.data.model.hackathon

data class Question(
    val questionId: String,
    val prid: String,
    val question: String,
    val answerOptions: List<AnswerOption>,
    val firstSegmentId: String,
)
