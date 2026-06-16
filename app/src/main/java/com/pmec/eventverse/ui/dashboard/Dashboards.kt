package com.pmec.eventverse.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StudentDashboard(onLogout: () -> Unit) {
    DashboardPlaceholder(title = "Student Dashboard", onLogout = onLogout)
}

@Composable
fun OrganizerDashboard(onLogout: () -> Unit) {
    DashboardPlaceholder(title = "Organizer Dashboard", onLogout = onLogout)
}

@Composable
fun AdminDashboard(onLogout: () -> Unit) {
    DashboardPlaceholder(title = "Admin Dashboard", onLogout = onLogout)
}

@Composable
fun DashboardPlaceholder(title: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}