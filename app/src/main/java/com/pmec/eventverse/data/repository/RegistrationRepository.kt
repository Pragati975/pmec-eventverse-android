package com.pmec.eventverse.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pmec.eventverse.data.model.Registration
import kotlinx.coroutines.tasks.await

class RegistrationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val registrationsCollection = db.collection("registrations")
    private val eventsCollection = db.collection("events")

    suspend fun registerForEvent(registration: Registration): Result<String> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val eventRef = db.collection("events").document(registration.eventId)
            val registrationsRef = db.collection("registrations")

            // Check duplicate first
            val existing = registrationsRef
                .whereEqualTo("eventId", registration.eventId)
                .whereEqualTo("userId", registration.userId)
                .whereEqualTo("status", "CONFIRMED")
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.failure(Exception("You are already registered!"))
            }

            // Use atomic transaction for seat count
            var registrationId = ""
            db.runTransaction { transaction ->
                val eventSnapshot = transaction.get(eventRef)
                val maxParticipants = eventSnapshot.getLong("maxParticipants")?.toInt() ?: 0
                val currentRegistrations = eventSnapshot.getLong("currentRegistrations")?.toInt() ?: 0

                if (currentRegistrations >= maxParticipants) {
                    throw Exception("Sorry, this event is full!")
                }

                // Create registration document
                val docRef = registrationsRef.document()
                registrationId = docRef.id
                val qrData = "PMEC_${registration.eventId}_${registration.userId}_${docRef.id}"
                val finalRegistration = registration.copy(
                    registrationId = docRef.id,
                    qrCode = qrData
                )

                transaction.set(docRef, finalRegistration)
                transaction.update(eventRef, "currentRegistrations", currentRegistrations + 1)
            }.await()

            Result.success(registrationId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // KEY FIX — uses doc.id to force correct registrationId
    suspend fun getUserRegistrations(userId: String): Result<List<Registration>> {
        return try {
            val snapshot = registrationsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val registrations = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Registration::class.java)?.copy(
                    registrationId = doc.id
                )
            }
            android.util.Log.d("REPO", "Found ${registrations.size} registrations for $userId")
            Result.success(registrations)
        } catch (e: Exception) {
            android.util.Log.e("REPO", "Error: ${e.message}")
            Result.failure(e)
        }
    }

    // KEY FIX — uses doc.id to force correct registrationId
    suspend fun getRegistrationForEvent(eventId: String, userId: String): Result<Registration?> {
        return try {
            val snapshot = registrationsCollection
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "CONFIRMED")
                .get()
                .await()

            if (snapshot.isEmpty) {
                Result.success(null)
            } else {
                val doc = snapshot.documents[0]
                val registration = doc.toObject(Registration::class.java)?.copy(
                    registrationId = doc.id
                )
                android.util.Log.d("REPO", "Found registration: ${doc.id}")
                Result.success(registration)
            }
        } catch (e: Exception) {
            android.util.Log.e("REPO", "Error: ${e.message}")
            Result.failure(e)
        }
    }

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

    suspend fun cancelRegistration(registrationId: String, eventId: String): Result<Unit> {
        return try {
            android.util.Log.d("REPO", "Cancelling: $registrationId")
            registrationsCollection.document(registrationId)
                .update("status", "CANCELLED")
                .await()

            val eventDoc = eventsCollection.document(eventId).get().await()
            val currentRegistrations = eventDoc.getLong("currentRegistrations")?.toInt() ?: 0
            if (currentRegistrations > 0) {
                eventsCollection.document(eventId)
                    .update("currentRegistrations", currentRegistrations - 1)
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("REPO", "Cancel error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getEventRegistrations(eventId: String): Result<List<Registration>> {
        return try {
            val snapshot = registrationsCollection
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "CONFIRMED")
                .get()
                .await()
            Result.success(snapshot.documents.mapNotNull { doc ->
                doc.toObject(Registration::class.java)?.copy(registrationId = doc.id)
            })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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