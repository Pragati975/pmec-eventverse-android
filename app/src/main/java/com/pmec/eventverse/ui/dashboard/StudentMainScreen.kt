package com.pmec.eventverse.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pmec.eventverse.data.model.Event
import com.pmec.eventverse.ui.events.HomeFeedScreen
import com.pmec.eventverse.ui.events.MyRegistrationsScreen
import com.pmec.eventverse.ui.notifications.NotificationsScreen
import com.pmec.eventverse.ui.profile.ProfileScreen
import com.pmec.eventverse.ui.theme.*

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun StudentMainScreen(
    onLogout: () -> Unit,
    onEventClick: (Event) -> Unit = {},
    onChatClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {}
) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, "home"),
        BottomNavItem("My Tickets", Icons.Default.ConfirmationNumber, "tickets"),
        BottomNavItem("Profile", Icons.Default.Person, "profile")
    )

    var selectedRoute by remember { mutableStateOf("home") }
    var userName by remember { mutableStateOf("Student") }
    var confirmedCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                userName = doc.getString("name") ?: "Student"
            }

        FirebaseFirestore.getInstance()
            .collection("registrations")
            .whereEqualTo("userId", uid)
            .whereEqualTo("status", "CONFIRMED")
            .get()
            .addOnSuccessListener { snap ->
                confirmedCount = snap.size()
            }
    }

    // Notifications screen is shown as a full-screen overlay outside the
    // bottom-nav Scaffold so it gets its own back button/top bar.
    if (selectedRoute == "notifications") {
        NotificationsScreen(
            onBack = { selectedRoute = "home" }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextPrimary
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (item.route == "tickets" && confirmedCount > 0) {
                                        Badge(containerColor = AccentBlue) {
                                            Text("$confirmedCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(item.icon, contentDescription = item.label)
                            }
                        },
                        label = { Text(item.label) },
                        selected = selectedRoute == item.route,
                        onClick = { selectedRoute = item.route },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = AccentBlue.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedRoute) {
            "home" -> Box(modifier = Modifier.fillMaxSize()) {
                HomeFeedScreen(
                    userName = userName,
                    onEventClick = onEventClick,
                    onNotificationClick = { selectedRoute = "notifications" },
                    modifier = Modifier.padding(paddingValues)
                )
                FloatingActionButton(
                    onClick = onChatClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 88.dp, end = 16.dp)
                        .size(60.dp),
                    shape = CircleShape,
                    containerColor = AccentPurple,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp
                    )
                ) {
                    Text(
                        text = "🤖",
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            "tickets" -> MyRegistrationsScreen(
                onBack = { selectedRoute = "home" },
                modifier = Modifier.padding(paddingValues)
            )
            "profile" -> ProfileScreen(
                onLogout = onLogout,
                onEditProfile = { onEditProfileClick() },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}