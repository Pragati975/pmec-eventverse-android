package com.pmec.eventverse.data.model

data class Feedback(
    val feedbackId: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val sentiment: String = "NEUTRAL", // POSITIVE / NEGATIVE / NEUTRAL
    val sentimentScore: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)