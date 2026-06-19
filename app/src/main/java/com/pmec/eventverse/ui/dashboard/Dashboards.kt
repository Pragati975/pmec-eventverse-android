package com.pmec.eventverse.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.pmec.eventverse.data.model.Event
import com.pmec.eventverse.ui.events.EventViewModel
import com.pmec.eventverse.ui.events.HomeFeedScreen
import com.pmec.eventverse.ui.theme.AccentBlue
import com.pmec.eventverse.ui.theme.ErrorRed

@Composable
fun StudentDashboard(
    onLogout: () -> Unit,
    onEventClick: (Event) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        HomeFeedScreen(
            userName = "Pragati",
            onEventClick = onEventClick
        )
        FloatingActionButton(
            onClick = onLogout,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 56.dp, end = 16.dp)
                .size(40.dp),
            containerColor = ErrorRed,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun OrganizerDashboard(
    onLogout: () -> Unit,
    onCreateEvent: () -> Unit = {},
    onEventClick: (Event) -> Unit = {}
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
fun AdminDashboard(onLogout: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        HomeFeedScreen(
            userName = "Admin",
            onEventClick = {}
        )
        FloatingActionButton(
            onClick = onLogout,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 56.dp, end = 16.dp)
                .size(40.dp),
            containerColor = ErrorRed,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Logout",
                modifier = Modifier.size(20.dp))
        }
    }
}