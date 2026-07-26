package com.pmec.eventverse.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pmec.eventverse.data.model.Event
import com.pmec.eventverse.ui.admin.AdminPanelScreen
import com.pmec.eventverse.ui.events.HomeFeedScreen
import com.pmec.eventverse.ui.theme.*

@Composable
fun StudentDashboard(
    onLogout: () -> Unit,
    onEventClick: (Event) -> Unit = {},
    onMyRegistrations: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {}
) {
    StudentMainScreen(
        onLogout = onLogout,
        onEventClick = onEventClick,
        onChatClick = onChatClick,
        onEditProfileClick = onEditProfileClick
    )
}
@Composable
fun OrganizerDashboard(
    onLogout: () -> Unit,
    onCreateEvent: () -> Unit = {},
    onEventClick: (Event) -> Unit = {},
    onMyEvents: () -> Unit = {},
    onScanQR: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        HomeFeedScreen(
            userName = "Organizer",
            onEventClick = onEventClick
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(
                onClick = onLogout,
                containerColor = ErrorRed,
                contentColor = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout",
                    modifier = Modifier.size(20.dp))
            }
            FloatingActionButton(
                onClick = onScanQR,
                containerColor = SuccessGreen,
                contentColor = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR",
                    modifier = Modifier.size(20.dp))
            }
            FloatingActionButton(
                onClick = onMyEvents,
                containerColor = AccentPurple,
                contentColor = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Event, contentDescription = "My Events",
                    modifier = Modifier.size(20.dp))
            }
            FloatingActionButton(
                onClick = onCreateEvent,
                containerColor = AccentBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Event")
            }
        }
    }
}

@Composable
fun AdminDashboard(
    onLogout: () -> Unit,
    onEventClick: (Event) -> Unit = {}
) {
    AdminPanelScreen(
        onLogout = onLogout,
        onEventClick = onEventClick
    )
}