package com.example.model

enum class HostRole(val displayName: String) {
    OMB_HOST("OMB Host"),
    TOURNAMENT_HOST("Tournament Host")
}

enum class HostStatus(val displayName: String) {
    ACTIVE("Active"),
    DISABLED("Disabled")
}

data class HostUser(
    val id: String,
    val fullName: String,
    val mobileNumber: String,
    val role: HostRole,
    val status: HostStatus,
    val currentAssignmentId: String? = null,
    val currentAssignmentType: String? = null // "OMB" or "TOURNAMENT"
)
