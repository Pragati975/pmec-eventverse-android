package com.pmec.eventverse.data.model

data class Event(
    val eventId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val date: Long = 0L,
    val time: String = "",
    val venue: String = "",
    val organizerId: String = "",
    val organizerName: String = "",
    val maxParticipants: Int = 0,
    val currentRegistrations: Int = 0,
    val posterUrl: String = "",
    val status: String = "UPCOMING",
    val tags: List<String> = emptyList(),
    val isApproved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)