package nl.npo.player.sampleApp.shared.presentation.game

sealed class ProgressState(
    val order: Int,
) {
    data object Init : ProgressState(Int.MIN_VALUE)

    data object Loading : ProgressState(1)

    data object AnswerQuestion : ProgressState(2)

    data class AnswerResult(
        val correct: Boolean,
    ) : ProgressState(3)

    data object GameEnded : ProgressState(4)

    data class Error(
        val errorMessage: String?,
    ) : ProgressState(5)

    fun isWithin(
        start: ProgressState,
        end: ProgressState,
    ) = order in start.order..end.order

    fun isBefore(other: ProgressState) = order < other.order

    fun isAfter(other: ProgressState) = order > other.order
}
