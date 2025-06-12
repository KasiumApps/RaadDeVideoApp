package nl.npo.player.sampleApp.shared.data.model.hackathon

data class Segment(
    val segmentId: String,
    val startTime: Int,
    val endTime: Int,
    val nextSegmentId: String?,
    val hasMoreSegments: Boolean,
)

fun Segment.toSegment(): nl.npo.player.library.domain.streamLink.model.Segment =
    nl.npo.player.library.domain.streamLink.model.Segment(
        segmentId,
        startTime,
        endTime,
    )
