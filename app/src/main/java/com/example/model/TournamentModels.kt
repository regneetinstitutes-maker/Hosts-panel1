package com.example.model

enum class TournamentStatus(val label: String) {
    AVAILABLE("Available"),
    RUNNING("Running"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class TournamentPrizeTier(
    val rank: Int,
    val prize: Double
)

data class CompetitorStanding(
    val position: Int,
    val gameId: String,
    val metricValue: Int,
    val atTime: String
)

data class TournamentSchedule(
    val id: String,
    val name: String,
    val date: String,
    val time: String,
    val isSubmitted: Boolean = false,
    val submittedAt: Long? = null,
    val participantValues: Map<String, Int> = emptyMap(), // participantId -> current metric value
    val standings: List<CompetitorStanding> = emptyList()
)

data class TournamentParticipant(
    val id: String,
    val name: String,
    val inGameUid: String,
    val gameIdName: String,
    val initialValue: Int? = null,
    val initialValueSavedAt: Long? = null, // Locks the start timestamp
    val finalValue: Int? = null,
    val finalValueSavedAt: Long? = null,
    val performance: Int? = null, // Final Value - Initial Value
    val rank: Int? = null,
    val isHackerCheater: Boolean = false,
    val hackerTaggedAt: Long? = null,
    val prizeAwarded: Double = 0.0,
    val refundAmount: Double = 0.0
)

data class Tournament(
    val id: String,
    val game: String,
    val mode: String,
    val tournamentMetric: String, // e.g. "Eliminations", "Score", "Wins"
    val entryFee: Double,
    val entryCloseTime: String,
    val durationMinutes: Int = 30,
    val resultsOn: String = "Today at 8:30 PM",
    val participantsJoined: Int,
    val maxParticipants: Int = 50,
    val teamSize: String = "Solo",
    val prizeChart: List<TournamentPrizeTier> = listOf(
        TournamentPrizeTier(1, 1500.0),
        TournamentPrizeTier(2, 900.0),
        TournamentPrizeTier(3, 600.0)
    ),
    val notes: String? = "Live eliminations tracked from official in-game match logs.",
    val status: TournamentStatus = TournamentStatus.AVAILABLE,
    val claimedByHostId: String? = null,
    val claimedByHostName: String? = null,
    val claimedAt: Long? = null,
    val schedules: List<TournamentSchedule> = emptyList(),
    val participants: List<TournamentParticipant> = emptyList()
)
