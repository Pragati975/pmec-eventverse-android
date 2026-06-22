package com.pmec.eventverse.ui.events

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pmec.eventverse.data.model.Event
import com.pmec.eventverse.data.repository.EventRepository
import kotlinx.coroutines.launch

sealed class EventState {
    object Idle : EventState()
    object Loading : EventState()
    data class Success(val message: String = "") : EventState()
    data class Error(val message: String) : EventState()
}

class EventViewModel : ViewModel() {
    private val repository = EventRepository()

    var eventState = mutableStateOf<EventState>(EventState.Idle)
    var events = mutableStateOf<List<Event>>(emptyList())
    var filteredEvents = mutableStateOf<List<Event>>(emptyList())
    var selectedCategory = mutableStateOf("ALL")

    fun createEvent(event: Event) {
        viewModelScope.launch {
            eventState.value = EventState.Loading
            val result = repository.createEvent(event)
            eventState.value = if (result.isSuccess)
                EventState.Success("Event created! Waiting for admin approval.")
            else
                EventState.Error(result.exceptionOrNull()?.message ?: "Failed to create event")
        }
    }

    fun createEventWithImage(event: Event, imageUri: android.net.Uri?) {
        viewModelScope.launch {
            eventState.value = EventState.Loading
            try {
                var finalEvent = event
                if (imageUri != null) {
                    val cloudinaryRepo = com.pmec.eventverse.data.repository.CloudinaryRepository()
                    val uploadResult = cloudinaryRepo.uploadImage(imageUri)
                    if (uploadResult.isSuccess) {
                        finalEvent = event.copy(posterUrl = uploadResult.getOrNull() ?: "")
                    }
                }
                val result = repository.createEvent(finalEvent)
                eventState.value = if (result.isSuccess)
                    EventState.Success("Event created! Waiting for admin approval.")
                else
                    EventState.Error(result.exceptionOrNull()?.message ?: "Failed to create event")
            } catch (e: Exception) {
                eventState.value = EventState.Error(e.message ?: "Upload failed")
            }
        }
    }

    fun loadApprovedEvents() {
        viewModelScope.launch {
            eventState.value = EventState.Loading
            val result = repository.getApprovedEvents()
            if (result.isSuccess) {
                events.value = result.getOrNull() ?: emptyList()
                filteredEvents.value = events.value
                eventState.value = EventState.Idle
            } else {
                eventState.value = EventState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load events"
                )
            }
        }
    }

    fun loadOrganizerEvents(organizerId: String) {
        viewModelScope.launch {
            eventState.value = EventState.Loading
            val result = repository.getOrganizerEvents(organizerId)
            if (result.isSuccess) {
                events.value = result.getOrNull() ?: emptyList()
                filteredEvents.value = events.value
                eventState.value = EventState.Idle
            } else {
                eventState.value = EventState.Error("Failed")
            }
        }
    }

    fun loadAllEvents() {
        viewModelScope.launch {
            eventState.value = EventState.Loading
            val result = repository.getAllEvents()
            if (result.isSuccess) {
                events.value = result.getOrNull() ?: emptyList()
                filteredEvents.value = events.value
                eventState.value = EventState.Idle
            } else {
                eventState.value = EventState.Error("Failed")
            }
        }
    }

    fun filterByCategory(category: String) {
        selectedCategory.value = category
        filteredEvents.value = if (category == "ALL")
            events.value
        else
            events.value.filter { it.category == category }
    }

    fun approveEvent(eventId: String) {
        viewModelScope.launch {
            repository.approveEvent(eventId)
            loadAllEvents()
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
            loadAllEvents()
        }
    }

    fun resetState() {
        eventState.value = EventState.Idle
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            eventState.value = EventState.Loading
            val result = repository.updateEvent(event)
            eventState.value = if (result.isSuccess)
                EventState.Success("Event updated successfully!")
            else
                EventState.Error(result.exceptionOrNull()?.message ?: "Failed to update event")
        }
    }
}