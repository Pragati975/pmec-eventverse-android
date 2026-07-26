package com.pmec.eventverse.ui.feedback

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pmec.eventverse.data.model.Feedback
import com.pmec.eventverse.data.repository.FeedbackRepository
import com.pmec.eventverse.data.repository.SentimentSummary
import kotlinx.coroutines.launch

sealed class FeedbackState {
    object Idle : FeedbackState()
    object Loading : FeedbackState()
    object Analyzing : FeedbackState()
    data class Success(val message: String = "") : FeedbackState()
    data class Error(val message: String) : FeedbackState()
}

class FeedbackViewModel : ViewModel() {
    private val repository = FeedbackRepository()

    var feedbackState = mutableStateOf<FeedbackState>(FeedbackState.Idle)
    var feedbacks = mutableStateOf<List<Feedback>>(emptyList())
    var sentimentSummary = mutableStateOf<SentimentSummary?>(null)

    fun submitFeedback(
        eventId: String,
        eventTitle: String,
        userId: String,
        userName: String,
        rating: Int,
        comment: String
    ) {
        viewModelScope.launch {
            feedbackState.value = FeedbackState.Analyzing

            // Step 1 — Analyze sentiment with Gemini
            val (sentiment, score, reason) = repository.analyzeSentiment(comment, rating)
            android.util.Log.d("SENTIMENT", "Result: $sentiment ($score) — $reason")

            // Step 2 — Create feedback object
            val feedback = Feedback(
                eventId = eventId,
                eventTitle = eventTitle,
                userId = userId,
                userName = userName,
                rating = rating,
                comment = comment,
                sentiment = sentiment,
                sentimentScore = score
            )

            // Step 3 — Submit to Firestore
            val result = repository.submitFeedback(feedback)
            feedbackState.value = if (result.isSuccess)
                FeedbackState.Success("Feedback submitted! Sentiment: $sentiment")
            else
                FeedbackState.Error(result.exceptionOrNull()?.message ?: "Failed to submit")
        }
    }

    fun loadEventFeedback(eventId: String) {
        viewModelScope.launch {
            feedbackState.value = FeedbackState.Loading
            val result = repository.getEventFeedback(eventId)
            if (result.isSuccess) {
                feedbacks.value = result.getOrNull() ?: emptyList()
                feedbackState.value = FeedbackState.Idle
            } else {
                feedbackState.value = FeedbackState.Error("Failed to load feedback")
            }
        }
    }

    fun loadSentimentSummary(eventId: String) {
        viewModelScope.launch {
            val result = repository.getEventSentimentSummary(eventId)
            if (result.isSuccess) {
                sentimentSummary.value = result.getOrNull()
            }
        }
    }

    fun resetState() {
        feedbackState.value = FeedbackState.Idle
    }
}