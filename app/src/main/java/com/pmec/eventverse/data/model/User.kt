package com.pmec.eventverse.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val department: String = "",
    val year: String = "",
    val rollNumber: String = "",
    val interests: List<String> = emptyList(),
    val profileImageUrl: String = "",
    val points: Int = 0,
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis()
)