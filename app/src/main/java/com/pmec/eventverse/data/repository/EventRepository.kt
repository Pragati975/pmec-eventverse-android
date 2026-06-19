package com.pmec.eventverse.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pmec.eventverse.data.model.Event
import kotlinx.coroutines.tasks.await

class EventRepository {
    private val db = FirebaseFirestore.getInstance()
    private val eventsCollection = db.collection("events")

    suspend fun createEvent(event: Event): Result<String> {
        return try {
            val docRef = eventsCollection.document()
            val eventWithId = event.copy(eventId = docRef.id)
            docRef.set(eventWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getApprovedEvents(): Result<List<Event>> {
        return try {
            val snapshot = eventsCollection
                .whereEqualTo("approved", true)
                .whereEqualTo("status", "UPCOMING")
                .get()
                .await()
            Result.success(snapshot.toObjects(Event::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllEvents(): Result<List<Event>> {
        return try {
            val snapshot = eventsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            Result.success(snapshot.toObjects(Event::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrganizerEvents(organizerId: String): Result<List<Event>> {
        return try {
            val snapshot = eventsCollection
                .whereEqualTo("organizerId", organizerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            Result.success(snapshot.toObjects(Event::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveEvent(eventId: String): Result<Unit> {
        return try {
            eventsCollection.document(eventId)
                .update("approved", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            eventsCollection.document(eventId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEvent(event: Event): Result<Unit> {
        return try {
            eventsCollection.document(event.eventId).set(event).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}