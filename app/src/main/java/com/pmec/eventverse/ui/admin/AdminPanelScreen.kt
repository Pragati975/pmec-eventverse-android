package com.pmec.eventverse.ui.admin

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pmec.eventverse.data.model.Event
import com.pmec.eventverse.ui.events.EventViewModel
import com.pmec.eventverse.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onLogout: () -> Unit,
    onEventClick: (Event) -> Unit = {}
) {
    val eventViewModel: EventViewModel = viewModel()
    val events by eventViewModel.events
    val eventState by eventViewModel.eventState

    LaunchedEffect(Unit) {
        eventViewModel.loadAllEvents()
    }

    val pendingEvents = events.filter { !it.approved }
    val approvedEvents = events.filter { it.approved }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Admin Dashboard",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = ErrorRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Events",
                        value = "${events.size}",
                        color = AccentBlue
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Pending",
                        value = "${pendingEvents.size}",
                        color = WarningYellow
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Approved",
                        value = "${approvedEvents.size}",
                        color = SuccessGreen
                    )
                }
            }

            // Pending Approval Section
            if (pendingEvents.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(WarningYellow, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Pending Approval (${pendingEvents.size})",
                            color = WarningYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                items(pendingEvents) { event ->
                    AdminEventCard(
                        event = event,
                        isPending = true,
                        onApprove = {
                            eventViewModel.approveEvent(event.eventId)
                        },
                        onDelete = {
                            eventViewModel.deleteEvent(event.eventId)
                        },
                        onClick = { onEventClick(event) }
                    )
                }
            }

            // Approved Events Section
            if (approvedEvents.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SuccessGreen, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Approved Events (${approvedEvents.size})",
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                items(approvedEvents) { event ->
                    AdminEventCard(
                        event = event,
                        isPending = false,
                        onApprove = {},
                        onDelete = {
                            eventViewModel.deleteEvent(event.eventId)
                        },
                        onClick = { onEventClick(event) }
                    )
                }
            }

            // Empty state
            if (events.isEmpty() && eventState !is com.pmec.eventverse.ui.events.EventState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📋", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No events yet", color = TextSecondary, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                color = color,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                title,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun AdminEventCard(
    event: Event,
    isPending: Boolean,
    onApprove: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Event", color = TextPrimary) },
            text = { Text("Are you sure you want to delete '${event.title}'?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        event.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "by ${event.organizerName}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                // Status badge
                Box(
                    modifier = Modifier
                        .background(
                            if (isPending) WarningYellow.copy(alpha = 0.2f)
                            else SuccessGreen.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (isPending) "PENDING" else "APPROVED",
                        color = if (isPending) WarningYellow else SuccessGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category chip
                Box(
                    modifier = Modifier
                        .background(SurfaceDark, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(event.category, color = AccentBlue, fontSize = 11.sp)
                }

                Text(
                    "${event.maxParticipants} seats",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                // Action buttons
                if (isPending) {
                    IconButton(
                        onClick = onApprove,
                        modifier = Modifier
                            .size(36.dp)
                            .background(SuccessGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Approve",
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .background(ErrorRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ErrorRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}