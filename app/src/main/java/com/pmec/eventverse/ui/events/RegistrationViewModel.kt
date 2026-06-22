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
            registrationState.value = if (result.isSuccess)
                RegistrationState.Success("Successfully registered! 🎉")
            else
                RegistrationState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
        }
    }

    fun checkRegistration(eventId: String, userId: String) {
        viewModelScope.launch {
            val result = repository.isUserRegistered(eventId, userId)
            if (result.isSuccess) {
                isRegistered.value = result.getOrNull() ?: false
                // Get registration ID if registered
                if (isRegistered.value) {
                    val regResult = repository.getUserRegistrations(userId)
                    if (regResult.isSuccess) {
                        val reg = regResult.getOrNull()?.find {
                            it.eventId == eventId && it.status == "CONFIRMED"
                        }
                        currentRegistrationId.value = reg?.registrationId ?: ""
                    }
                }
            }
        }
    }

    fun loadUserRegistrations(userId: String) {
        viewModelScope.launch {
            registrationState.value = RegistrationState.Loading
            val result = repository.getUserRegistrations(userId)
            if (result.isSuccess) {
                registrations.value = result.getOrNull() ?: emptyList()
                registrationState.value = RegistrationState.Idle
            } else {
                registrationState.value = RegistrationState.Error("Failed to load registrations")
            }
        }
    }

    fun cancelRegistration(registrationId: String, eventId: String) {
        viewModelScope.launch {
            registrationState.value = RegistrationState.Loading
            val result = repository.cancelRegistration(registrationId, eventId)
            registrationState.value = if (result.isSuccess) {
                isRegistered.value = false
                RegistrationState.Success("Registration cancelled")
            } else {
                RegistrationState.Error(result.exceptionOrNull()?.message ?: "Failed to cancel")
            }
        }
    }

    fun resetState() {
        registrationState.value = RegistrationState.Idle
    }
}