package com.pmec.eventverse.ui.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pmec.eventverse.data.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    val repository = AuthRepository()
    var authState = mutableStateOf<AuthState>(AuthState.Idle)

    fun signUp(
        name: String, email: String, password: String,
        role: String, department: String, year: String, rollNumber: String
    ) {
        viewModelScope.launch {
            authState.value = AuthState.Loading
            val result = repository.signUp(name, email, password, role, department, year, rollNumber)
            authState.value = if (result.isSuccess)
                AuthState.Success(result.getOrNull()!!)
            else
                AuthState.Error(result.exceptionOrNull()?.message ?: "Something went wrong")
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            authState.value = AuthState.Loading
            val result = repository.login(email, password)
            authState.value = if (result.isSuccess)
                AuthState.Success(result.getOrNull()!!)
            else
                AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            val result = repository.forgotPassword(email)
            authState.value = if (result.isSuccess)
                AuthState.Error("Password reset email sent! Check your inbox.")
            else
                AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to send reset email")
        }
    }

    fun resetState() {
        authState.value = AuthState.Idle
    }
}