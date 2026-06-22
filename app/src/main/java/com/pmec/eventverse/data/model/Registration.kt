package com.pmec.eventverse.data.model

data class Registration(
    val registrationId: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val eventDate: Long = 0L,
    val eventVenue: String = "",
    val eventCategory: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userRollNumber: String = "",
    val status: String = "CONFIRMED", // CONFIRMED / CANCELLED / WAITLISTED
    val qrCode: String = "",
    val attended: Boolean = false,
    val registeredAt: Long = System.currentTimeMillis()
)