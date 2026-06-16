package com.pmec.eventverse.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pmec.eventverse.data.repository.AuthRepository
import com.pmec.eventverse.ui.auth.AuthViewModel
import com.pmec.eventverse.ui.auth.LoginScreen
import com.pmec.eventverse.ui.auth.SignUpScreen
import com.pmec.eventverse.ui.dashboard.AdminDashboard
import com.pmec.eventverse.ui.dashboard.OrganizerDashboard
import com.pmec.eventverse.ui.dashboard.StudentDashboard
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val repository = AuthRepository()

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
            StudentDashboard(onLogout = {
                repository.logout()
                navController.navigate("login") { popUpTo(0) { inclusive = true } }
            })
        }

        composable("organizer_dashboard") {
            OrganizerDashboard(onLogout = {
                repository.logout()
                navController.navigate("login") { popUpTo(0) { inclusive = true } }
            })
        }

        composable("admin_dashboard") {
            AdminDashboard(onLogout = {
                repository.logout()
                navController.navigate("login") { popUpTo(0) { inclusive = true } }
            })
        }
    }
}