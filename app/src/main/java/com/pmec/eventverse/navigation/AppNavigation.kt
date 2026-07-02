package com.pmec.eventverse.navigation

import androidx.compose.runtime.*
import com.pmec.eventverse.ui.scanner.QRScannerScreen
import com.pmec.eventverse.ui.events.MyRegistrationsScreen
import com.pmec.eventverse.ui.events.MyEventsScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pmec.eventverse.data.model.Event
import com.pmec.eventverse.data.repository.AuthRepository
import com.pmec.eventverse.ui.auth.AuthViewModel
import com.pmec.eventverse.ui.auth.LoginScreen
import com.pmec.eventverse.ui.auth.SignUpScreen
import com.pmec.eventverse.ui.dashboard.AdminDashboard
import com.pmec.eventverse.ui.dashboard.OrganizerDashboard
import com.pmec.eventverse.ui.dashboard.StudentDashboard
import com.pmec.eventverse.ui.events.CreateEventScreen
import com.pmec.eventverse.ui.events.EventDetailScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val repository = AuthRepository()

    // Shared selected event state
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var eventToEdit by remember { mutableStateOf<Event?>(null) }

    fun navigateToDashboard(role: String) {
        val destination = when (role.uppercase()) {
            "STUDENT" -> "student_dashboard"
            "ORGANIZER" -> "organizer_dashboard"
            "ADMIN" -> "admin_dashboard"
            else -> "student_dashboard"
        }
        navController.navigate(destination) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate("signup") },
                onLoginSuccess = { role -> navigateToDashboard(role) }
            )
        }

        composable("signup") {
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = { role -> navigateToDashboard(role) }
            )
        }
        composable("qr_scanner") {
            QRScannerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("student_dashboard") {
            StudentDashboard(
                onLogout = {
                    repository.logout()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onEventClick = { event ->
                    selectedEvent = event
                    navController.navigate("event_detail")
                },
                onMyRegistrations = {
                    navController.navigate("my_registrations")
                }
            )
        }

        composable("organizer_dashboard") {
            OrganizerDashboard(
                onLogout = {
                    repository.logout()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onCreateEvent = { navController.navigate("create_event") },
                onEventClick = { event ->
                    selectedEvent = event
                    navController.navigate("event_detail")
                },
                onMyEvents = { navController.navigate("my_events") },
                onScanQR = { navController.navigate("qr_scanner") }
            )
        }

        composable("admin_dashboard") {
            AdminDashboard(
                onLogout = {
                    repository.logout()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onEventClick = { event ->
                    selectedEvent = event
                    navController.navigate("event_detail")
                }
            )
        }
        composable("create_event") {
            CreateEventScreen(
                onBack = {
                    eventToEdit = null
                    navController.popBackStack()
                },
                onEventCreated = {
                    eventToEdit = null
                    navController.popBackStack()
                },
                eventToEdit = eventToEdit
            )
        }
        composable("my_registrations") {
            MyRegistrationsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("my_events") {
            MyEventsScreen(
                onBack = { navController.popBackStack() },
                onEditEvent = { event ->
                    eventToEdit = event
                    navController.navigate("create_event")
                },
                onEventClick = { event ->
                    selectedEvent = event
                    navController.navigate("event_detail")
                }
            )
        }
        composable("event_detail") {
            selectedEvent?.let { event ->
                EventDetailScreen(
                    event = event,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}