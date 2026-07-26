package com.pmec.eventverse.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GeminiRepository {

    private val apiKey = BuildConfig.GEMINI_API_KEY // Paste correct key here

    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    private val db = FirebaseFirestore.getInstance()

    private suspend fun getEventsContext(): String {
        return try {
            val snapshot = db.collection("events")
                .whereEqualTo("approved", true)
                .get()
                .await()

            if (snapshot.isEmpty) {
                "No events are currently available."
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val category = doc.getString("category") ?: ""
                    val date = doc.getLong("date")?.let {
                        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                            .format(java.util.Date(it))
                    } ?: ""
                    val venue = doc.getString("venue") ?: ""
                    val description = doc.getString("description") ?: ""
                    val seats = doc.getLong("maxParticipants")?.toInt() ?: 0
                    val registered = doc.getLong("currentRegistrations")?.toInt() ?: 0
                    val seatsLeft = seats - registered
                    "Event: $title | Category: $category | Date: $date | Venue: $venue | Seats Left: $seatsLeft | Description: $description"
                }.joinToString("\n")
            }
        } catch (e: Exception) {
            android.util.Log.e("GEMINI", "getEventsContext error: ${e.message}")
            "Unable to fetch events at this moment."
        }
    }

    suspend fun chat(userMessage: String): Result<String> {
        return try {
            val eventsContext = getEventsContext()

            val systemPrompt = """
                You are EventBot, a helpful AI assistant for PMEC EventVerse — a college event management app for Parala Maharaja Engineering College (PMEC) in Odisha, India.
                
                Your job is to help students:
                - Find events they might be interested in
                - Answer questions about upcoming events
                - Help them decide which events to register for
                - Provide information about event dates, venues, and categories
                - Give friendly, concise answers
                
                Here are the current events at PMEC:
                $eventsContext
                
                Rules:
                - Keep responses short and friendly (2-4 sentences max)
                - If asked about events, refer to the list above
                - If no relevant events exist, say so honestly
                - Don't make up events that aren't in the list
                - Use emojis occasionally to be friendly
                - Always respond in English
                - If asked something unrelated to events, gently redirect to event-related topics
            """.trimIndent()

            val fullPrompt = "$systemPrompt\n\nStudent: $userMessage\nEventBot:"

            val response = model.generateContent(fullPrompt)
            val text = response.text ?: "Sorry, I couldn't generate a response."
            android.util.Log.d("GEMINI", "Response: $text")
            // Clean up markdown formatting
            val cleanText = text.trim()
                .replace("**", "")
                .replace("##", "")
                .replace("* ", "• ")
            Result.success(cleanText)
        } catch (e: Exception) {
            android.util.Log.e("GEMINI", "chat error: ${e.message} | cause: ${e.cause}")
            Result.failure(Exception("Failed: ${e.message}"))
        }
    }
}