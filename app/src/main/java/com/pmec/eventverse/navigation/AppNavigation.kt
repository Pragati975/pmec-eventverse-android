package com.pmec.eventverse.navigation

import androidx.compose.runtime.*
import androidx.compose.animation.*
import com.pmec.eventverse.ui.profile.EditProfileScreen
import com.pmec.eventverse.ui.feedback.FeedbackScreen
import com.pmec.eventverse.ui.feedback.SentimentDashboardScreen
import androidx.compose.animation.core.tween
import com.pmec.eventverse.ui.chat.ChatScreen
import com.pmec.eventverse.ui.scanner.QRScannerScreen
import com.pmec.eventverse.ui.events.MyRegistrationsScreen
import com.pmec.eventverse.ui.events.MyEventsScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import com.pmec.eventverse.ui.splash.SplashScreen
import androidx.compose.animation.core.tween
import androidx.compose.animation.ExperimentalAnimationApi
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
    var feedbackEventId by remember { mutableStateOf("") }
    var feedbackEventTitle by remember { mutableStateOf("") }
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

    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300))
        }
    ) {
        composable("splash") {
            SplashScreen(
                onSplashComplete = {
                    val currentUser = repository.getCurrentUser()
                    if (currentUser != null) {
                        // Already logged in — check role
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate("signup") },
                onLoginSuccess = { role -> navigateToDashboard(role) }
            )
        }
        composable("chat") {
            ChatScreen(
                onBack = { navController.popBackStack() }
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
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onEventClick = { event ->
                    selectedEvent = event
                    navController.navigate("event_detail")
                },
                onMyRegistrations = {
                    navController.navigate("my_registrations")
                },
                onChatClick = {
                    navController.navigate("chat")
                },
                onEditProfileClick = {
                    navController.navigate("edit_profile")
                }
            )
        }
        composable("edit_profile") {
            EditProfileScreen(onBack = { navController.popBackStack() })
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
                },
                onViewFeedback = { event ->
                    feedbackEventId = event.eventId
                    feedbackEventTitle = event.title
                    navController.navigate("sentiment_dashboard")
                }
            )
        }
        composable("feedback") {
            FeedbackScreen(
                eventId = feedbackEventId,
                eventTitle = feedbackEventTitle,
                onBack = { navController.popBackStack() }
            )
        }

        composable("sentiment_dashboard") {
            SentimentDashboardScreen(
                eventId = feedbackEventId,
                eventTitle = feedbackEventTitle,
                onBack = { navController.popBackStack() }
            )
        }
        composable("event_detail") {
            selectedEvent?.let { event ->
                EventDetailScreen(
                    event = event,
                    onBack = { navController.popBackStack() },
                    onFeedback = {
                        feedbackEventId = event.eventId
                        feedbackEventTitle = event.title
                        navController.navigate("feedback")
                    }
                )
            }
        }

    }

}

