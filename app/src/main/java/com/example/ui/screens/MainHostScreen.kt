package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.model.HostRole
import com.example.model.HostStatus
import com.example.ui.components.*
import com.example.ui.screens.omb.OmbAvailableScreen
import com.example.ui.screens.omb.OmbRunningScreen
import com.example.ui.screens.tournament.TournamentAvailableScreen
import com.example.ui.screens.tournament.TournamentRunningScreen
import com.example.ui.viewmodel.HostTab
import com.example.ui.viewmodel.HostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHostScreen(
    viewModel: HostViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val host = uiState.currentHost ?: return

    var showNotificationDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short,
                withDismissAction = true
            )
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short,
                withDismissAction = true
            )
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (host.role == HostRole.OMB_HOST) MaterialTheme.colorScheme.primaryContainer else Color(0xFF10B981).copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (host.role == HostRole.OMB_HOST) Icons.Default.Gamepad else Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = if (host.role == HostRole.OMB_HOST) MaterialTheme.colorScheme.primary else Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = host.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (host.status == HostStatus.ACTIVE) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = host.status.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = host.role.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Push Notification Button with Badge
                    IconButton(
                        onClick = { showNotificationDialog = true },
                        modifier = Modifier.testTag("notification_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (uiState.notifications.isNotEmpty()) {
                                    Badge { Text("${uiState.notifications.size}") }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }

                    // Logout Button
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // OPERATIONAL ALERT BANNER (If any alerts from Manager / Admin)
            OperationalAlertBanner(alerts = uiState.operationalAlerts)

            // TABS: AVAILABLE vs RUNNING
            TabRow(
                selectedTabIndex = if (uiState.selectedTab == HostTab.AVAILABLE) 0 else 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("host_tab_row")
            ) {
                Tab(
                    selected = uiState.selectedTab == HostTab.AVAILABLE,
                    onClick = { viewModel.selectTab(HostTab.AVAILABLE) },
                    text = {
                        val count = if (host.role == HostRole.OMB_HOST) uiState.availableOmbs.size else uiState.availableTournaments.size
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Available", fontWeight = FontWeight.Bold)
                            if (count > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_available")
                )
                Tab(
                    selected = uiState.selectedTab == HostTab.RUNNING,
                    onClick = { viewModel.selectTab(HostTab.RUNNING) },
                    text = {
                        val hasRunning = (host.role == HostRole.OMB_HOST && uiState.runningOmb != null) ||
                                         (host.role == HostRole.TOURNAMENT_HOST && uiState.runningTournament != null)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Running", fontWeight = FontWeight.Bold)
                            if (hasRunning) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF10B981), CircleShape)
                                )
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_running")
                )
            }

            // BODY CONTENT BY ROLE & TAB
            Box(modifier = Modifier.weight(1f)) {
                if (host.role == HostRole.OMB_HOST) {
                    when (uiState.selectedTab) {
                        HostTab.AVAILABLE -> OmbAvailableScreen(uiState = uiState, viewModel = viewModel)
                        HostTab.RUNNING -> OmbRunningScreen(match = uiState.runningOmb, uiState = uiState, viewModel = viewModel)
                    }
                } else {
                    when (uiState.selectedTab) {
                        HostTab.AVAILABLE -> TournamentAvailableScreen(uiState = uiState, viewModel = viewModel)
                        HostTab.RUNNING -> TournamentRunningScreen(tournament = uiState.runningTournament, uiState = uiState, viewModel = viewModel)
                    }
                }

                // Global loading indicator
                if (uiState.isLoading) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Card(shape = RoundedCornerShape(16.dp)) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = uiState.loadingMessage.ifBlank { "Processing request..." },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOGS

    // 1. Notification Dialog
    if (showNotificationDialog) {
        NotificationDialog(
            notifications = uiState.notifications,
            onClaimFromNotification = { matchId, type ->
                if (type == "OMB") {
                    viewModel.claimOmb(matchId)
                } else {
                    viewModel.claimTournament(matchId)
                }
            },
            onDismiss = { showNotificationDialog = false }
        )
    }
}

