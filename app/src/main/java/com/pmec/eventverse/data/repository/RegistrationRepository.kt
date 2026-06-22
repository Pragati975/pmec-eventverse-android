package com.pmec.eventverse.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pmec.eventverse.data.model.Registration
import kotlinx.coroutines.tasks.await

class RegistrationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val registrationsCollection = db.collection("registrations")
    private val eventsCollection = db.collection("events")

    // Register for event
    suspend fun registerForEvent(registration: Registration): Result<String> {
        return try {
            // Check if already registered
            val existing = registrationsCollection
                .whereEqualTo("eventId", registration.eventId)
                .whereEqualTo("userId", registration.userId)
                .whereEqualTo("status", "CONFIRMED")
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.failure(Exception("You are already registered for this event!"))
            }

            // Check seats available
            val eventDoc = eventsCollection.document(registration.eventId).get().await()
            val maxParticipants = eventDoc.getLong("maxParticipants")?.toInt() ?: 0
            val currentRegistrations = eventDoc.getLong("currentRegistrations")?.toInt() ?: 0

            if (currentRegistrations >= maxParticipants) {
                return Result.failure(Exception("Sorry, this event is full!"))
            }

            // Create registration
            val docRef = registrationsCollection.document()
            val qrData = "PMEC_${registration.eventId}_${registration.userId}_${docRef.id}"
            val finalRegistration = registration.copy(
                registrationId = docRef.id,
                qrCode = qrData
            )
            docRef.set(finalRegistration).await()

            // Increment currentRegistrations
            eventsCollection.document(registration.eventId)
                .update("currentRegistrations", currentRegistrations + 1)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user registrations
    suspend fun getUserRegistrations(userId: String): Result<List<Registration>> {
        return try {
            val snapshot = registrationsCollection
                .whereEqualTo("userId", userId)
                .orderBy("registeredAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            Result.success(snapshot.toObjects(Registration::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Check if user registered for specific event
    suspend fun isUserRegistered(eventId: String, userId: String): Result<Boolean> {
        return try {
            val snapshot = registrationsCollection
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "CONFIRMED")
                .get()
                .await()
            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cancel registration
    suspend fun cancelRegistration(registrationId: String, eventId: String): Result<Unit> {
        return try {
            // Update registration status
            registrationsCollection.document(registrationId)
                .update("status", "CANCELLED")
                .await()

            // Decrement currentRegistrations
            val eventDoc = eventsCollection.document(eventId).get().await()
            val currentRegistrations = eventDoc.getLong("currentRegistrations")?.toInt() ?: 0
            if (currentRegistrations > 0) {
                eventsCollection.document(eventId)
                    .update("currentRegistrations", currentRegistrations - 1)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get event registrations (for organizer/admin)
    suspend fun getEventRegistrations(eventId: String): Result<List<Registration>> {
        return try {
            val snapshot = registrationsCollection
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "CONFIRMED")
                .get()
                .await()
            Result.success(snapshot.toObjects(Registration::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mark attendance via QR
    suspend fun markAttendance(registrationId: String): Result<Unit> {
        return try {
            registrationsCollection.document(registrationId)
                .update("attended", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}