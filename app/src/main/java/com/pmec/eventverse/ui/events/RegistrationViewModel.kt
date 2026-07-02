package com.pmec.eventverse.ui.events

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pmec.eventverse.data.model.Registration
import com.pmec.eventverse.data.repository.RegistrationRepository
import kotlinx.coroutines.launch

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val message: String = "") : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

class RegistrationViewModel : ViewModel() {
    private val repository = RegistrationRepository()

    var registrationState = mutableStateOf<RegistrationState>(RegistrationState.Idle)
    var registrations = mutableStateOf<List<Registration>>(emptyList())
    var isRegistered = mutableStateOf(false)
    var currentRegistrationId = mutableStateOf("")

    fun registerForEvent(registration: Registration) {
        viewModelScope.launch {
            registrationState.value = RegistrationState.Loading
            val result = repository.registerForEvent(registration)
            if (result.isSuccess) {
                // After successful registration, reload registration status
                checkRegistration(registration.eventId, registration.userId)
                registrationState.value = RegistrationState.Success("Successfully registered! 🎉")
            } else {
                registrationState.value = RegistrationState.Error(
                    result.exceptionOrNull()?.message ?: "Registration failed"
                )
            }
        }
    }

    fun checkRegistration(eventId: String, userId: String) {
        viewModelScope.launch {
            try {
                val result = repository.getRegistrationForEvent(eventId, userId)
                if (result.isSuccess) {
                    val registration = result.getOrNull()
                    if (registration != null) {
                        isRegistered.value = true
                        currentRegistrationId.value = registration.registrationId
                        android.util.Log.d("REG_CHECK", "Found registration ID: ${registration.registrationId}")
                    } else {
                        isRegistered.value = false
                        currentRegistrationId.value = ""
                        android.util.Log.d("REG_CHECK", "No registration found")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("REG_CHECK", "Error: ${e.message}")
                isRegistered.value = false
            }
        }
    }

    fun loadUserRegistrations(userId: String) {
        viewModelScope.launch {
            registrationState.value = RegistrationState.Loading
            val result = repository.getUserRegistrations(userId)
            if (result.isSuccess) {
                val regs = result.getOrNull() ?: emptyList()
                android.util.Log.d("REG_LOAD", "Loaded ${regs.size} registrations")
                registrations.value = regs
                registrationState.value = RegistrationState.Idle
            } else {
                android.util.Log.e("REG_LOAD", "Error: ${result.exceptionOrNull()?.message}")
                registrationState.value = RegistrationState.Error("Failed to load registrations")
            }
        }
    }

    fun cancelRegistration(registrationId: String, eventId: String) {
        viewModelScope.launch {
            android.util.Log.d("REG_CANCEL", "Cancelling registration ID: $registrationId")
            if (registrationId.isEmpty()) {
                registrationState.value = RegistrationState.Error("Registration ID not found")
                return@launch
            }
            registrationState.value = RegistrationState.Loading
            val result = repository.cancelRegistration(registrationId, eventId)
            if (result.isSuccess) {
                isRegistered.value = false
                currentRegistrationId.value = ""
                registrationState.value = RegistrationState.Success("Registration cancelled")
            } else {
                registrationState.value = RegistrationState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to cancel"
                )
            }
        }
    }

    fun resetState() {
        registrationState.value = RegistrationState.Idle
    }
}