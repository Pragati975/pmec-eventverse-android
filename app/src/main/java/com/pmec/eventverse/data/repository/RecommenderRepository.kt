package com.pmec.eventverse.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pmec.eventverse.data.model.Event
import kotlinx.coroutines.tasks.await

class RecommenderRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getRecommendedEvents(userId: String): Result<List<Event>> {
        return try {
            // Get user's past registrations
            val registrations = db.collection("registrations")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            android.util.Log.d("RECOMMENDER", "User registrations: ${registrations.size()}")

            // Get ALL approved events (no compound query to avoid index issues)
            val allEventsSnapshot = db.collection("events")
                .get()
                .await()

            android.util.Log.d("RECOMMENDER", "Total events fetched: ${allEventsSnapshot.size()}")

            val allEvents = allEventsSnapshot.documents.mapNotNull { doc ->
                val event = doc.toObject(Event::class.java)?.copy(eventId = doc.id)
                // Only include approved events
                if (event?.approved == true) event else null
            }

            android.util.Log.d("RECOMMENDER", "Approved events: ${allEvents.size}")

            if (registrations.isEmpty) {
                // New user — return most popular events
                val popular = allEvents.sortedByDescending { it.currentRegistrations }.take(5)
                android.util.Log.d("RECOMMENDER", "Returning ${popular.size} popular events")
                return Result.success(popular)
            }

            // Build category interest profile
            val categoryCount = mutableMapOf<String, Int>()
            registrations.documents.forEach { doc ->
                val category = doc.getString("eventCategory") ?: return@forEach
                categoryCount[category] = (categoryCount[category] ?: 0) + 1
            }

            android.util.Log.d("RECOMMENDER", "Category profile: $categoryCount")

            val topCategories = categoryCount.entries
                .sortedByDescending { it.value }
                .take(2)
                .map { it.key }

            val registeredEventIds = registrations.documents
                .mapNotNull { it.getString("eventId") }
                .toSet()

            // Score events based on category match
            val scoredEvents = allEvents
                .filter { it.eventId !in registeredEventIds }
                .map { event ->
                    val score = when {
                        topCategories.isNotEmpty() && event.category == topCategories[0] -> 3
                        topCategories.size > 1 && event.category == topCategories[1] -> 2
                        else -> 1
                    }
                    Pair(event, score)
                }
                .sortedByDescending { it.second }
                .map { it.first }

            android.util.Log.d("RECOMMENDER", "Scored events: ${scoredEvents.size}")
            Result.success(scoredEvents.take(5))

        } catch (e: Exception) {
            android.util.Log.e("RECOMMENDER", "Error: ${e.message}")
            Result.failure(e)
        }
    }
}