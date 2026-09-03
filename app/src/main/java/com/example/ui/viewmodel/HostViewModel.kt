package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import com.example.network.HostRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class HostTab {
    AVAILABLE,
    RUNNING
}

data class HostUiState(
    val currentHost: HostUser? = null,
    val selectedTab: HostTab = HostTab.AVAILABLE,
    val availableOmbs: List<OmbMatch> = emptyList(),
    val availableTournaments: List<Tournament> = emptyList(),
    val runningOmb: OmbMatch? = null,
    val runningTournament: Tournament? = null,
    val isLoading: Boolean = false,
    val loadingMessage: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val participantSearchQuery: String = "",
    val operationalAlerts: List<OperationalAlert> = emptyList(),
    val notifications: List<HostNotification> = emptyList()
)

class HostViewModel(
    val repository: HostRepository = HostRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HostUiState())
    val uiState: StateFlow<HostUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        // Collect host user state
        viewModelScope.launch {
            repository.currentHost.collect { host ->
                _uiState.update { it.copy(currentHost = host) }
                if (host != null) {
                    refreshData()
                    startRealtimeSync()
                } else {
                    stopRealtimeSync()
                }
            }
        }

        // Collect operational alerts
        viewModelScope.launch {
            repository.operationalAlerts.collect { alerts ->
                _uiState.update { it.copy(operationalAlerts = alerts) }
            }
        }

        // Collect notifications
        viewModelScope.launch {
            repository.notifications.collect { notif ->
                _uiState.update {
                    it.copy(notifications = listOf(notif) + it.notifications)
                }
            }
        }

        // Sync repository ombs & tournaments to UI state
        viewModelScope.launch {
            repository.ombsState.collect { ombMap ->
                val host = _uiState.value.currentHost
                val available = ombMap.values.filter { it.status == MatchStatus.AVAILABLE }.sortedBy { it.id }
                val running = host?.currentAssignmentId?.let { id ->
                    ombMap[id]?.takeIf { it.status == MatchStatus.RUNNING || it.status == MatchStatus.CANCELLED || it.status == MatchStatus.COMPLETED }
                }
                _uiState.update {
                    it.copy(
                        availableOmbs = available,
                        runningOmb = running
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.tournamentsState.collect { tourneyMap ->
                val host = _uiState.value.currentHost
                val available = tourneyMap.values.filter { it.status == TournamentStatus.AVAILABLE }.sortedBy { it.id }
                val running = host?.currentAssignmentId?.let { id ->
                    tourneyMap[id]?.takeIf { it.status == TournamentStatus.RUNNING || it.status == TournamentStatus.CANCELLED || it.status == TournamentStatus.COMPLETED }
                }
                _uiState.update {
                    it.copy(
                        availableTournaments = available,
                        runningTournament = running
                    )
                }
            }
        }
    }

    private fun startRealtimeSync() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(3000) // 3-second gentle heartbeat sync with live backend
                repository.syncAlertsAndNotifications()
                refreshData(silent = true)
            }
        }
    }

    private fun stopRealtimeSync() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun selectTab(tab: HostTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setParticipantSearchQuery(query: String) {
        _uiState.update { it.copy(participantSearchQuery = query) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun login(identifier: String, password: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Authenticating with server...", errorMessage = null) }
            val result = repository.login(identifier, password)
            _uiState.update { it.copy(isLoading = false) }
            result.onSuccess { host ->
                _uiState.update {
                    it.copy(
                        successMessage = "Welcome, ${host.fullName}! Logged in as ${host.role.displayName}.",
                        selectedTab = if (host.currentAssignmentId != null) HostTab.RUNNING else HostTab.AVAILABLE
                    )
                }
                refreshData()
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Authentication failed.") }
            }
        }
    }

    fun logout() {
        repository.logout()
        _uiState.update {
            HostUiState(
                currentHost = null,
                selectedTab = HostTab.AVAILABLE
            )
        }
    }

    fun refreshData(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(isLoading = true, loadingMessage = "Syncing with live server...") }
            }
            repository.refreshHostProfile()
            val host = repository.currentHost.value
            if (host != null) {
                if (host.role == HostRole.OMB_HOST) {
                    val ombsRes = repository.getAvailableOmbs()
                    ombsRes.onSuccess { list ->
                        _uiState.update { it.copy(availableOmbs = list) }
                    }
                    if (host.currentAssignmentId != null) {
                        val runningRes = repository.getOmb(host.currentAssignmentId)
                        runningRes.onSuccess { match ->
                            _uiState.update { it.copy(runningOmb = match) }
                        }
                    }
                } else if (host.role == HostRole.TOURNAMENT_HOST) {
                    val tourneysRes = repository.getAvailableTournaments()
                    tourneysRes.onSuccess { list ->
                        _uiState.update { it.copy(availableTournaments = list) }
                    }
                    if (host.currentAssignmentId != null) {
                        val runningRes = repository.getTournament(host.currentAssignmentId)
                        runningRes.onSuccess { tourney ->
                            _uiState.update { it.copy(runningTournament = tourney) }
                        }
                    }
                }
            }
            if (!silent) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ----------------------------------------------------
    // OMB ACTIONS
    // ----------------------------------------------------
    fun claimOmb(ombId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Claiming match #$ombId...") }
            val res = repository.claimOmb(ombId)
            _uiState.update { it.copy(isLoading = false) }
            res.onSuccess { match ->
                _uiState.update {
                    it.copy(
                        runningOmb = match,
                        selectedTab = HostTab.RUNNING,
                        successMessage = "Match #$ombId successfully claimed! Assignment moved to Running."
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to claim match.") }
            }
        }
    }

    fun submitOmbRoomDetails(
        ombId: String,
        firstRoomId: String,
        firstPassword: String,
        secondRoomId: String,
        secondPassword: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Validating Room ID & Password...") }
            val res = repository.submitOmbRoomDetails(ombId, firstRoomId, firstPassword, secondRoomId, secondPassword)
            _uiState.update { it.copy(isLoading = false) }
            res.onSuccess { updated ->
                _uiState.update {
                    it.copy(
                        runningOmb = updated,
                        successMessage = "Room details submitted successfully."
                    )
                }
                onSuccess()
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Verification failed.") }
            }
        }
    }

    fun confirmParticipantRoom(ombId: String, participantId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Confirming participant room entry...") }
            val res = repository.confirmParticipantRoom(ombId, participantId)
            _uiState.update { it.copy(isLoading = false) }
            res.onSuccess { updated ->
                _uiState.update {
                    it.copy(
                        runningOmb = updated,
                        successMessage = "Participant room entry confirmed."
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to confirm participant.") }
            }
        }
    }

    fun assignParticipantPosition(
        ombId: String,
        participantId: String,
        firstPos: Int,
        secondPos: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Verifying and saving position...") }
            val res = repository.assignParticipantPosition(ombId, participantId, firstPos, secondPos)
            _uiState.update { it.copy(isLoading = false) }
            res.onSuccess { updated ->
                _uiState.update {
                    it.copy(
                        runningOmb = updated,
                        successMessage = "Position $firstPos successfully saved for participant."
                    )
                }
                onSuccess()
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Verification failed.") }
            }
        }
    }

    fun tagParticipantHacker(
        assignmentId: String,
        participantId: String,
        assignmentType: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Applying Hacker / Cheater penalty...") }
            val res = repository.tagParticipantHacker(assignmentId, participantId, assignmentType)
            _uiState.update { it.copy(isLoading = false) }
            res.onSuccess {
                _uiState.update {
                    it.copy(
                        successMessage = "Participant tagged as Hacker/Cheater. Prize & Refund set to 0."
                    )
                }
                refreshData(silent = true)
                onSuccess()
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to tag participant.") }
            }
        }
    }

    fun uploadOmbScreenshot(ombId: String, screenshotUrl: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Uploading match result screenshot...") }
            val res = repository.uploadOmbScreenshot(ombId, screenshotUrl)
            _uiState.update { it.copy(isLoading = false) }
            res.onSuccess { updated ->
                _uiState.update {
                    it.copy(
                        runningOmb = updated,
                        successMessage = "Screenshot Uploaded ✅"
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Screenshot upload failed.") }
            }
        }
    }

    fun submitOmbResult(ombId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Submitting result & processing prize distribution...") }
            val res = repository.submitOmbResult(ombId)
            _uiState.update { it.copy(isLoading = false) }
            res.onSuccess { updated ->
                _uiState.update {
                    it.copy(
                        runningOmb = null,
                        selectedTab = HostTab.AVAILABLE,
                        successMessage = "Result Submitted! Prize distribution processed automatically by backend."
                    )
                }
                refreshData()
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Result submission failed.") }
            }
        }
    }

    // ----------------------------------------------------
    // TOURNAMENT ACTIONS
    // ----------------------------------------------------
    fun claimTournament(tournamentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Claiming tournament #$tournamentId...") }
            val res = repository.claimTournament(tournamentId)
            _uiState.update { it.copy(isLoading = false) }
            res.onSuccess { tourney ->
                _uiState.update {
                    it.copy(
                        runningTournament = tourney,
                        selectedTab = HostTab.RUNNING,
                        successMessage = "Tournament #$tournamentId successfully claimed! Moved to Running."
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to claim tournament.") }
            }
        }
    }

    fun saveTournamentInitialValue(
        tournamentId: String,
        participantId: String,
        firstVal: Int,
        secondVal: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Validating initial metric value...") }
            val res = repository.saveTournamentInitialValue(tournamentId, participantId, firstVal, secondVal)
            _uiState.update { it.copy(isLoading = false) }
            res.onSuccess { updated ->
                _uiState.update {
                    it.copy(
                        runningTournament = updated,
                        successMessage = "Initial Value Saved. Start timestamp locked."
                    )
                }
                onSuccess()
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Verification failed.") }
            }
        }
    }

    fun saveTournamentFinalValue(
        tournamentId: String,
        participantId: String,
        firstVal: Int,
        secondVal: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Calculating performance & standings...") }
            val res = repository.saveTournamentFinalValue(tournamentId, participantId, firstVal, secondVal)
            _uiState.update { it.copy(isLoading = false) }
            res.onSuccess { updated ->
                _uiState.update {
                    it.copy(
                        runningTournament = updated,
                        successMessage = "Final Value Saved. Performance and live rank updated."
                    )
                }
                onSuccess()
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Verification failed.") }
            }
        }
    }

    fun submitScheduleValues(
        tournamentId: String,
        scheduleId: String,
        values: Map<String, Int>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Publishing schedule standings chart...") }
            val res = repository.submitScheduleValues(tournamentId, scheduleId, values)
            _uiState.update { it.copy(isLoading = false) }
            res.onSuccess { updated ->
                _uiState.update {
                    it.copy(
                        runningTournament = updated,
                        successMessage = "Schedule standings chart successfully generated and published!"
                    )
                }
                onSuccess()
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to submit schedule.") }
            }
        }
    }

    // ----------------------------------------------------
    // RELEASE ASSIGNMENT
    // ----------------------------------------------------
    fun releaseAssignment(
        assignmentId: String,
        assignmentType: String,
        confirmationText: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Releasing assignment back to Available pool...") }
            val res = repository.releaseAssignment(assignmentId, assignmentType, confirmationText)
            _uiState.update { it.copy(isLoading = false) }
            res.onSuccess {
                _uiState.update {
                    it.copy(
                        runningOmb = null,
                        runningTournament = null,
                        selectedTab = HostTab.AVAILABLE,
                        successMessage = "Assignment released. Moved from Running to Available."
                    )
                }
                refreshData()
                onSuccess()
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to release assignment.") }
            }
        }
    }
}

