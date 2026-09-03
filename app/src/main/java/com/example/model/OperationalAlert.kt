package com.example.model

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class OperationalAlert(
    val id: String,
    val matchOrTournamentId: String,
    val hostId: String,
    val hostName: String,
    val matchDetails: String,
    val alertType: String,
    val message: String,
    val severity: AlertSeverity,
    val timestamp: Long = System.currentTimeMillis()
)

data class HostNotification(
    val id: String,
    val title: String,
    val matchId: String,
    val game: String,
    val mode: String,
    val entryFee: Double,
    val type: String, // "OMB" or "TOURNAMENT"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isClaimed: Boolean = false
)
