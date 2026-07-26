package com.pmec.eventverse.data.model

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String = "",
    val isFromUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false
)