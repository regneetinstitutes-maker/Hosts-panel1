package com.example.ui.screens.omb

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MatchStatus
import com.example.model.OmbMatch
import com.example.model.OmbParticipant
import com.example.ui.components.*
import com.example.ui.viewmodel.HostUiState
import com.example.ui.viewmodel.HostViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OmbRunningScreen(
    match: OmbMatch?,
    uiState: HostUiState,
    viewModel: HostViewModel,
    modifier: Modifier = Modifier
) {
    if (match == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("omb_no_running_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Running Assignment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Go to the Available tab to claim an active match.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    // State holders for dialogs
    var showRoomDialog by remember { mutableStateOf(false) }
    var showReleaseDialog by remember { mutableStateOf(false) }
    var showScreenshotDialog by remember { mutableStateOf(false) }
    var participantToConfirmRoom by remember { mutableStateOf<OmbParticipant?>(null) }
    var participantForPosition by remember { mutableStateOf<OmbParticipant?>(null) }
    var participantForHackerTag by remember { mutableStateOf<OmbParticipant?>(null) }

    val searchQuery = uiState.participantSearchQuery
    val filteredParticipants = remember(match.participants, searchQuery) {
        if (searchQuery.isBlank()) {
            match.participants
        } else {
            val q = searchQuery.trim().lowercase()
            match.participants.filter {
                it.name.lowercase().contains(q) ||
                it.gameIdName.lowercase().contains(q) ||
                it.inGameUid.lowercase().contains(q)
            }
        }
    }

    val isCancelled = match.status == MatchStatus.CANCELLED
    val isCompleted = match.status == MatchStatus.COMPLETED

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("omb_running_screen")
    ) {
        // TOP APP BAR / RUNNING HEADER
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Running OMB",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = when (match.status) {
                            MatchStatus.RUNNING -> Color(0xFF10B981)
                            MatchStatus.CANCELLED -> MaterialTheme.colorScheme.error
                            MatchStatus.COMPLETED -> Color(0xFF38BDF8)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = match.status.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Match ID: #${match.id} • ${match.game} (${match.mode})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // RELEASE BUTTON
            if (!isCancelled && !isCompleted) {
                OutlinedButton(
                    onClick = { showReleaseDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("omb_release_button")
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
            // CANCELLATION NOTICE IF CANCELLED
            if (isCancelled) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MATCH CANCELLED & REFUNDED",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = match.cancellationReason ?: "This match has been cancelled by backend automation and 100% refund has been processed for all participants.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // 1. MATCH DETAILS CARD (Admin Configured)
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
                            text = "Admin Configured Match Information",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = "Entry Fee", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "₹${match.entryFee.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(text = "Prize Pool", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "₹${match.prizePool.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                            Column {
                                Text(text = "Team Size", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = match.teamSize, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text(text = "Start Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = match.startTime, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = "Reveal Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = match.roomRevealTime, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column {
                                Text(text = "Result Deadline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = match.resultDeadline, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                            }
                            Column {
                                Text(text = "Entry Close", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = match.entryCloseTime, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                        }

                        if (!match.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Notes: ${match.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. ROOM DETAILS & DOUBLE VERIFICATION SECTION
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (match.roomId != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Room Details",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Reveal Time: ${match.roomRevealTime} (Admin Schedule)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (match.roomId != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "ROOM ID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = match.roomId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                    }
                                    Divider(modifier = Modifier.height(36.dp).width(1.dp))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "PASSWORD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = match.roomPassword ?: "—", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                    }
                                    Divider(modifier = Modifier.height(36.dp).width(1.dp))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "STATUS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "Submitted ✅", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Submit Room ID & Password using double-entry verification.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showRoomDialog = true },
                                enabled = !isCancelled && !isCompleted,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_room_details_button")
                            ) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enter Room ID & Password (Double Entry)")
                            }
                        }
                    }
                }
            }

            // 3. SCREENSHOT UPLOAD SECTION
            item {
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
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Match Result Screenshot",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Overall in-game match result scoreboard",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (match.screenshotUrl != null) {
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Screenshot Uploaded ✅", fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                        Text(text = match.screenshotUrl, style = MaterialTheme.typography.bodySmall, color = Color(0xFF047857))
                                    }
                                    TextButton(onClick = { showScreenshotDialog = true }) {
                                        Text("Replace")
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { showScreenshotDialog = true },
                                enabled = !isCancelled && !isCompleted,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("upload_screenshot_button")
                            ) {
                                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Screenshot")
                            }
                        }
                    }
                }
            }

            // 4. PARTICIPANTS SECTION WITH PARTIAL SEARCH
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Participants (${match.participants.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Partial Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setParticipantSearchQuery(it) },
                        placeholder = { Text("Search participant (e.g. 'ha' for Rohan, Harsh)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setParticipantSearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Search")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("participant_search_input")
                    )
                }
            }

            // PARTICIPANTS LIST
            items(filteredParticipants, key = { it.id }) { participant ->
                OmbParticipantItem(
                    participant = participant,
                    isInteractive = !isCancelled && !isCompleted,
                    onRoomConfirmClicked = { participantToConfirmRoom = participant },
                    onPositionClicked = { participantForPosition = participant },
                    onHackerTagClicked = { participantForHackerTag = participant }
                )
            }

            // 5. RESULT SUBMISSION BUTTON
            if (!isCancelled && !isCompleted) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.submitOmbResult(match.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_omb_result_button")
                    ) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Match Result & Distribute Prizes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // DIALOGS

    // 1. Room Double Verification Dialog
    if (showRoomDialog) {
        DoubleVerificationDialog(
            title = "Submit Room ID & Password",
            primaryFieldLabel = "Room ID",
            secondaryFieldLabel = "Room Password",
            isSecret = false,
            onDismiss = { showRoomDialog = false },
            onConfirmed = { f1, f2, s1, s2 ->
                viewModel.submitOmbRoomDetails(match.id, f1, f2, s1, s2) {
                    showRoomDialog = false
                }
            }
        )
    }

    // 2. Room Confirmation Dialog (Prevents single accidental click)
    participantToConfirmRoom?.let { participant ->
        AlertDialog(
            onDismissRequest = { participantToConfirmRoom = null },
            icon = {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
            },
            title = { Text("Confirm Room Entry") },
            text = {
                Text("Confirm that participant '${participant.name}' (${participant.gameIdName}) has joined the in-game room?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmParticipantRoom(match.id, participant.id)
                        participantToConfirmRoom = null
                    },
                    modifier = Modifier.testTag("confirm_room_dialog_button")
                ) {
                    Text("Confirm Entry ✅")
                }
            },
            dismissButton = {
                TextButton(onClick = { participantToConfirmRoom = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. Position Double Verification Dialog
    participantForPosition?.let { participant ->
        DoubleVerificationDialog(
            title = "Assign Position: ${participant.name}",
            primaryFieldLabel = "Position (e.g. 1 for 1st, 2 for 2nd)",
            isNumeric = true,
            onDismiss = { participantForPosition = null },
            onConfirmed = { f1, _, s1, _ ->
                val p1 = f1.toIntOrNull() ?: 0
                val p2 = s1.toIntOrNull() ?: 0
                viewModel.assignParticipantPosition(match.id, participant.id, p1, p2) {
                    participantForPosition = null
                }
            }
        )
    }

    // 4. Hacker/Cheater Double Verification Dialog
    participantForHackerTag?.let { participant ->
        HackerTagDialog(
            participantName = participant.name,
            gameId = participant.gameIdName,
            onDismiss = { participantForHackerTag = null },
            onConfirmed = {
                viewModel.tagParticipantHacker(match.id, participant.id, "OMB") {
                    participantForHackerTag = null
                }
            }
        )
    }

    // 5. Screenshot Upload Dialog
    if (showScreenshotDialog) {
        ScreenshotUploadDialog(
            matchId = match.id,
            currentScreenshotUrl = match.screenshotUrl,
            onDismiss = { showScreenshotDialog = false },
            onUploadScreenshot = { url ->
                viewModel.uploadOmbScreenshot(match.id, url)
                showScreenshotDialog = false
            }
        )
    }

    // 6. Release Dialog
    if (showReleaseDialog) {
        ReleaseDialog(
            assignmentId = match.id,
            onDismiss = { showReleaseDialog = false },
            onConfirmRelease = { text ->
                viewModel.releaseAssignment(match.id, "OMB", text) {
                    showReleaseDialog = false
                }
            }
        )
    }
}

@Composable
fun OmbParticipantItem(
    participant: OmbParticipant,
    isInteractive: Boolean,
    onRoomConfirmClicked: () -> Unit,
    onPositionClicked: () -> Unit,
    onHackerTagClicked: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (participant.isHackerCheater) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("participant_item_${participant.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Room Confirmation Checkbox Box
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (participant.roomConfirmed) Color(0xFF10B981) else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (participant.roomConfirmed) Color(0xFF10B981) else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = isInteractive && !participant.roomConfirmed) {
                        onRoomConfirmClicked()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (participant.roomConfirmed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Room Confirmed",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(text = "⬜", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Participant Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = participant.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (participant.isHackerCheater) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "HACKER / CHEATER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "UID: ${participant.inGameUid} • Ign: ${participant.gameIdName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (participant.prizeAwarded > 0) {
                    Text(
                        text = "Prize: ₹${participant.prizeAwarded.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Position Box
            Surface(
                color = if (participant.position != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.clickable(enabled = isInteractive) {
                    onPositionClicked()
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (participant.position != null) "Pos: ${participant.position}" else "Pos: —",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (participant.position != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Hacker Tag Button
            if (isInteractive && !participant.isHackerCheater) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onHackerTagClicked,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Flag Hacker",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
