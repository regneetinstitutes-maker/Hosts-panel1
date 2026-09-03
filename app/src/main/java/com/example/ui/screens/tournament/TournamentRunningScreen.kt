package com.example.ui.screens.tournament

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.viewmodel.HostUiState
import com.example.ui.viewmodel.HostViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TournamentRunningScreen(
    tournament: Tournament?,
    uiState: HostUiState,
    viewModel: HostViewModel,
    modifier: Modifier = Modifier
) {
    if (tournament == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("tourney_no_running_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Running Tournament Assignment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Go to the Available tab to claim an active tournament.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    var showReleaseDialog by remember { mutableStateOf(false) }
    var participantForInitialVal by remember { mutableStateOf<TournamentParticipant?>(null) }
    var participantForFinalVal by remember { mutableStateOf<TournamentParticipant?>(null) }
    var participantForHackerTag by remember { mutableStateOf<TournamentParticipant?>(null) }
    var scheduleToFill by remember { mutableStateOf<TournamentSchedule?>(null) }

    val isCancelled = tournament.status == TournamentStatus.CANCELLED
    val isCompleted = tournament.status == TournamentStatus.COMPLETED

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("tournament_running_screen")
    ) {
        // TOP APP BAR / RUNNING HEADER
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Running Tournament",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = when (tournament.status) {
                            TournamentStatus.RUNNING -> Color(0xFF10B981)
                            TournamentStatus.CANCELLED -> MaterialTheme.colorScheme.error
                            TournamentStatus.COMPLETED -> Color(0xFF38BDF8)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = tournament.status.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Tournament ID: #${tournament.id} • ${tournament.game} (${tournament.mode})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!isCancelled && !isCompleted) {
                OutlinedButton(
                    onClick = { showReleaseDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("tournament_release_button")
                ) {
                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RELEASE")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. TOURNAMENT DETAILS CARD
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Admin Configured Tournament Details",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = "Metric", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = tournament.tournamentMetric, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                            Column {
                                Text(text = "Entry Fee", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "₹${tournament.entryFee.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(text = "Duration", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "${tournament.durationMinutes} mins", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text(text = "Results On", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = tournament.resultsOn, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Prize Chart preview
                        Text(
                            text = "Prize Chart: " + tournament.prizeChart.joinToString(", ") { "Rank ${it.rank}: ₹${it.prize.toInt()}" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. PARTICIPANTS METRIC TABLE (Initial & Final Values)
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Participants & Metric Tracking",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Metric: ${tournament.tournamentMetric}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            items(tournament.participants, key = { it.id }) { participant ->
                TournamentParticipantRow(
                    participant = participant,
                    metricName = tournament.tournamentMetric,
                    isInteractive = !isCancelled && !isCompleted,
                    onEnterInitialValue = { participantForInitialVal = participant },
                    onEnterFinalValue = { participantForFinalVal = participant },
                    onHackerTag = { participantForHackerTag = participant }
                )
            }

            // 3. COMPETITORS POSITION SCHEDULES SECTION
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Leaderboard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Competitor Position Schedules",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Admin configured interim reveal charts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        tournament.schedules.forEach { schedule ->
                            Surface(
                                color = if (schedule.isSubmitted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = schedule.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = "Time: ${schedule.time}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    if (schedule.isSubmitted && schedule.standings.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Published Competitor Standings Chart:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        schedule.standings.forEach { standing ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = "Pos #${standing.position} • ${standing.gameId}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                                Text(text = "${tournament.tournamentMetric}: ${standing.metricValue} (At ${standing.atTime})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { scheduleToFill = schedule },
                                            enabled = !isCancelled && !isCompleted,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(imageVector = Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Enter Values for ${schedule.name}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // DIALOGS

    // 1. Initial Value Double Verification
    participantForInitialVal?.let { participant ->
        DoubleVerificationDialog(
            title = "Enter Initial ${tournament.tournamentMetric}: ${participant.name}",
            primaryFieldLabel = "Initial ${tournament.tournamentMetric} Value",
            isNumeric = true,
            onDismiss = { participantForInitialVal = null },
            onConfirmed = { f1, _, s1, _ ->
                val v1 = f1.toIntOrNull() ?: 0
                val v2 = s1.toIntOrNull() ?: 0
                viewModel.saveTournamentInitialValue(tournament.id, participant.id, v1, v2) {
                    participantForInitialVal = null
                }
            }
        )
    }

    // 2. Final Value Double Verification
    participantForFinalVal?.let { participant ->
        DoubleVerificationDialog(
            title = "Enter Final ${tournament.tournamentMetric}: ${participant.name}",
            primaryFieldLabel = "Final ${tournament.tournamentMetric} Value",
            isNumeric = true,
            onDismiss = { participantForFinalVal = null },
            onConfirmed = { f1, _, s1, _ ->
                val v1 = f1.toIntOrNull() ?: 0
                val v2 = s1.toIntOrNull() ?: 0
                viewModel.saveTournamentFinalValue(tournament.id, participant.id, v1, v2) {
                    participantForFinalVal = null
                }
            }
        )
    }

    // 3. Hacker Tag Dialog
    participantForHackerTag?.let { participant ->
        HackerTagDialog(
            participantName = participant.name,
            gameId = participant.gameIdName,
            onDismiss = { participantForHackerTag = null },
            onConfirmed = {
                viewModel.tagParticipantHacker(tournament.id, participant.id, "TOURNAMENT") {
                    participantForHackerTag = null
                }
            }
        )
    }

    // 4. Schedule Entry Dialog (Single entry for schedule snapshot)
    scheduleToFill?.let { schedule ->
        ScheduleValuesEntryDialog(
            tournament = tournament,
            schedule = schedule,
            onDismiss = { scheduleToFill = null },
            onSubmitValues = { valuesMap ->
                viewModel.submitScheduleValues(tournament.id, schedule.id, valuesMap) {
                    scheduleToFill = null
                }
            }
        )
    }

    // 5. Release Dialog
    if (showReleaseDialog) {
        ReleaseDialog(
            assignmentId = tournament.id,
            onDismiss = { showReleaseDialog = false },
            onConfirmRelease = { text ->
                viewModel.releaseAssignment(tournament.id, "TOURNAMENT", text) {
                    showReleaseDialog = false
                }
            }
        )
    }
}

@Composable
fun TournamentParticipantRow(
    participant: TournamentParticipant,
    metricName: String,
    isInteractive: Boolean,
    onEnterInitialValue: () -> Unit,
    onEnterFinalValue: () -> Unit,
    onHackerTag: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (participant.isHackerCheater) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tourney_participant_${participant.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = participant.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (participant.rank != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF10B981),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Rank #${participant.rank}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (participant.isHackerCheater) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "HACKER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Game ID: ${participant.gameIdName} • UID: ${participant.inGameUid}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isInteractive && !participant.isHackerCheater) {
                    IconButton(onClick = onHackerTag, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.WarningAmber, contentDescription = "Tag Hacker", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metric tracking row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Initial Value Box
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = isInteractive && participant.initialValue == null) {
                            onEnterInitialValue()
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Initial $metricName", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (participant.initialValue != null) {
                            Text(text = "${participant.initialValue} ✅", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        } else {
                            Text(text = "+ Enter Value", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Final Value Box
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = isInteractive && participant.initialValue != null && participant.finalValue == null) {
                            onEnterFinalValue()
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Final $metricName", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (participant.finalValue != null) {
                            Text(text = "${participant.finalValue} ✅", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        } else if (participant.initialValue != null) {
                            Text(text = "+ Enter Final", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        } else {
                            Text(text = "Pending Initial", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                }

                // Performance Box
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Performance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (participant.performance != null) {
                            Text(
                                text = "+${participant.performance}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(text = "—", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleValuesEntryDialog(
    tournament: Tournament,
    schedule: TournamentSchedule,
    onDismiss: () -> Unit,
    onSubmitValues: (Map<String, Int>) -> Unit
) {
    val valuesState = remember {
        mutableStateMapOf<String, String>().apply {
            tournament.participants.forEach { p ->
                put(p.id, "")
            }
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Enter Standings for ${schedule.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Metric: ${tournament.tournamentMetric} (Single Entry Snapshot)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tournament.participants) { p ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = p.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(text = p.gameIdName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedTextField(
                                value = valuesState[p.id] ?: "",
                                onValueChange = { valuesState[p.id] = it },
                                placeholder = { Text("0") },
                                singleLine = true,
                                modifier = Modifier.width(90.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val resultMap = valuesState.mapValues { it.value.toIntOrNull() ?: 0 }
                            onSubmitValues(resultMap)
                        }
                    ) {
                        Text("Publish Chart")
                    }
                }
            }
        }
    }
}
