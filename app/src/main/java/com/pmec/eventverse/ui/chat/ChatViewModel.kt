package com.pmec.eventverse.ui.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pmec.eventverse.data.model.ChatMessage
import com.pmec.eventverse.data.repository.GeminiRepository
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = GeminiRepository()

    val messages = mutableStateListOf<ChatMessage>()
    var isLoading = mutableStateOf(false)

    init {
        // Welcome message
        messages.add(
            ChatMessage(
                message = "Hi! I'm EventBot 🎉 I can help you find events, answer questions about registrations, and tell you what's happening at PMEC. What would you like to know?",
                isFromUser = false
            )
        )
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        // Add user message
        messages.add(ChatMessage(message = userMessage, isFromUser = true))
        isLoading.value = true

        // Add loading placeholder
        val loadingMsg = ChatMessage(message = "...", isFromUser = false, isLoading = true)
        messages.add(loadingMsg)

        viewModelScope.launch {
            val result = repository.chat(userMessage)

            // Remove loading placeholder
            messages.removeIf { it.isLoading }
            isLoading.value = false

            if (result.isSuccess) {
                messages.add(
                    ChatMessage(
                        message = result.getOrNull() ?: "Sorry, something went wrong.",
                        isFromUser = false
                    )
                )
            } else {
                messages.add(
                    ChatMessage(
                        message = "Sorry, I'm having trouble connecting right now. Please try again! 😅",
                        isFromUser = false
                    )
                )
            }
        }
    }
}