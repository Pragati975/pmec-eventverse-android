package com.pmec.eventverse.navigation

import androidx.compose.runtime.*
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

        composable("student_dashboard") {
            StudentDashboard(
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
                }
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
                onBack = { navController.popBackStack() },
                onEventCreated = { navController.popBackStack() }
            )
        }

        composable("event_detail") {
            selectedEvent?.let { event ->
                EventDetailScreen(
                    event = event,
                    onBack = { navController.popBackStack() },
                    onRegister = {
                        // Registration logic coming in Week 3
                    }
                )
            }
        }
    }
}