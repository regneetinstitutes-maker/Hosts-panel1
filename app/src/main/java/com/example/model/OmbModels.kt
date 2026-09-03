package com.example.model

enum class MatchStatus(val label: String) {
    AVAILABLE("Available"),
    RUNNING("Running"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class OmbParticipant(
    val id: String,
    val name: String,
    val inGameUid: String,
    val gameIdName: String,
    val roomConfirmed: Boolean = false,
    val roomConfirmedAt: Long? = null,
    val roomConfirmedByHostId: String? = null,
    val position: Int? = null,
    val positionConfirmedAt: Long? = null,
    val isHackerCheater: Boolean = false,
    val hackerTaggedAt: Long? = null,
    val prizeAwarded: Double = 0.0,
    val refundAmount: Double = 0.0
)

data class OmbMatch(
    val id: String,
    val game: String,
    val mode: String,
    val entryFee: Double,
    val entryCloseTime: String,
    val startTime: String,
    val roomRevealTime: String,
    val resultDeadline: String,
    val participantsJoined: Int,
    val maxParticipants: Int = 100,
    val teamSize: String = "Solo",
    val prizePool: Double = 1000.0,
    val prizeDistribution: List<Pair<Int, Double>> = listOf(1 to 500.0, 2 to 300.0, 3 to 200.0),
    val guideVideoUrl: String? = "https://example.com/guide/bgmi-room-setup",
    val notes: String? = "Admin Note: Ensure all players enter in designated slot before start.",
    val status: MatchStatus = MatchStatus.AVAILABLE,
    val claimedByHostId: String? = null,
    val claimedByHostName: String? = null,
    val claimedAt: Long? = null,
    val roomId: String? = null,
    val roomPassword: String? = null,
    val roomDetailsSubmittedAt: Long? = null,
    val screenshotUrl: String? = null,
    val screenshotUploadedAt: Long? = null,
    val resultSubmittedAt: Long? = null,
    val cancellationReason: String? = null,
    val participants: List<OmbParticipant> = emptyList()
)
