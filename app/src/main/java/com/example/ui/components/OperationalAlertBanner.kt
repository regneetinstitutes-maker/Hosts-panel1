package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import com.example.model.AlertSeverity
import com.example.model.OperationalAlert
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OperationalAlertBanner(
    alerts: List<OperationalAlert>,
    modifier: Modifier = Modifier
) {
    if (alerts.isEmpty()) return

    var isExpanded by remember { mutableStateOf(false) }
    val latestAlert = alerts.first()

    Surface(
        color = when (latestAlert.severity) {
            AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
            AlertSeverity.WARNING -> Color(0xFFFEF3C7) // warm amber
            AlertSeverity.INFO -> MaterialTheme.colorScheme.secondaryContainer
        },
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("operational_alert_banner")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
            ) {
                Icon(
                    imageVector = when (latestAlert.severity) {
                        AlertSeverity.CRITICAL -> Icons.Default.Cancel
                        AlertSeverity.WARNING -> Icons.Default.Warning
                        AlertSeverity.INFO -> Icons.Default.Info
                    },
                    contentDescription = "Alert",
                    tint = when (latestAlert.severity) {
                        AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.error
                        AlertSeverity.WARNING -> Color(0xFFD97706)
                        AlertSeverity.INFO -> MaterialTheme.colorScheme.secondary
                    },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MANAGER / ADMIN ALERT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (latestAlert.severity) {
                                AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.error
                                AlertSeverity.WARNING -> Color(0xFFB45309)
                                AlertSeverity.INFO -> MaterialTheme.colorScheme.secondary
                            }
                        )
                        if (alerts.size > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text("${alerts.size}")
                            }
                        }
                    }
                    Text(
                        text = latestAlert.message,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = when (latestAlert.severity) {
                            AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.onErrorContainer
                            AlertSeverity.WARNING -> Color(0xFF78350F)
                            AlertSeverity.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2
                    )
                }
                IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Alerts",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Divider(color = Color.Black.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Alert Details Log (Source of Truth Sync):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    alerts.take(4).forEach { alert ->
                        val timeFormat = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
                        Text(
                            text = "• [${timeFormat.format(Date(alert.timestamp))}] ${alert.matchOrTournamentId} (${alert.matchDetails}) - ${alert.message}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
