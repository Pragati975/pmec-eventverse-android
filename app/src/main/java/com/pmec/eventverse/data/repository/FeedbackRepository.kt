package com.pmec.eventverse.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pmec.eventverse.data.model.Feedback
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class FeedbackRepository {
    private val db = FirebaseFirestore.getInstance()
    private val feedbackCollection = db.collection("feedback")
    private val apiKey = BuildConfig.GEMINI_API_KEY// same key as GeminiRepository

    // Analyze sentiment using Gemini
    suspend fun analyzeSentiment(comment: String, rating: Int): Triple<String, Float, String> {
        return try {
            val prompt = """
                Analyze the sentiment of this event feedback:
                Rating: $rating/5 stars
                Comment: "$comment"
                
                Respond with ONLY a JSON object in this exact format (no markdown, no extra text):
                {"sentiment": "POSITIVE", "score": 0.85, "reason": "brief reason"}
                
                Rules:
                - sentiment must be exactly: POSITIVE, NEGATIVE, or NEUTRAL
                - score is 0.0 to 1.0 (confidence)
                - reason is max 10 words
                - Consider both rating AND comment together
            """.trimIndent()

            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }.toString()

            connection.outputStream.use { it.write(requestBody.toByteArray()) }

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)
                val text = jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                val result = JSONObject(text)
                val sentiment = result.getString("sentiment")
                val score = result.getDouble("score").toFloat()
                val reason = result.getString("reason")
                Triple(sentiment, score, reason)
            } else {
                // Fallback: simple rule-based sentiment
                val sentiment = when {
                    rating >= 4 -> "POSITIVE"
                    rating <= 2 -> "NEGATIVE"
                    else -> "NEUTRAL"
                }
                Triple(sentiment, rating / 5f, "Based on rating")
            }
        } catch (e: Exception) {
            android.util.Log.e("SENTIMENT", "Error: ${e.message}")
            // Fallback sentiment based on rating
            val sentiment = when {
                rating >= 4 -> "POSITIVE"
                rating <= 2 -> "NEGATIVE"
                else -> "NEUTRAL"
            }
            Triple(sentiment, rating / 5f, "Based on rating")
        }
    }

    // Submit feedback
    suspend fun submitFeedback(feedback: Feedback): Result<Unit> {
        return try {
            // Check if already submitted
            val existing = feedbackCollection
                .whereEqualTo("eventId", feedback.eventId)
                .whereEqualTo("userId", feedback.userId)
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.failure(Exception("You have already submitted feedback for this event!"))
            }

            val docRef = feedbackCollection.document()
            val finalFeedback = feedback.copy(feedbackId = docRef.id)
            docRef.set(finalFeedback).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get feedback for an event
    suspend fun getEventFeedback(eventId: String): Result<List<Feedback>> {
        return try {
            val snapshot = feedbackCollection
                .whereEqualTo("eventId", eventId)
                .get()
                .await()
            val feedbacks = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Feedback::class.java)?.copy(feedbackId = doc.id)
            }
            Result.success(feedbacks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get sentiment summary for an event
    suspend fun getEventSentimentSummary(eventId: String): Result<SentimentSummary> {
        return try {
            val feedbacks = getEventFeedback(eventId).getOrNull() ?: emptyList()
            if (feedbacks.isEmpty()) {
                return Result.success(SentimentSummary(0, 0, 0, 0, 0f))
            }

            val positive = feedbacks.count { it.sentiment == "POSITIVE" }
            val negative = feedbacks.count { it.sentiment == "NEGATIVE" }
            val neutral = feedbacks.count { it.sentiment == "NEUTRAL" }
            val avgRating = feedbacks.map { it.rating }.average().toFloat()
            val total = feedbacks.size

            Result.success(SentimentSummary(total, positive, negative, neutral, avgRating))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class SentimentSummary(
    val total: Int,
    val positive: Int,
    val negative: Int,
    val neutral: Int,
    val averageRating: Float
)