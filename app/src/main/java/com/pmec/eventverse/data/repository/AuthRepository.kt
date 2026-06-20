package com.pmec.eventverse.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pmec.eventverse.data.model.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        role: String,
        department: String,
        year: String,
        rollNumber: String
    ): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user!!.uid

            val user = User(
                uid = uid,
                name = name,
                email = email,
                role = role,
                department = department,
                year = year,
                rollNumber = rollNumber
            )

            db.collection("users").document(uid).set(user).await()
            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user!!.uid

            // Add timeout — if Firestore takes too long, default to role from cache
            val role = try {
                withTimeout(5000L) {
                    val doc = db.collection("users").document(uid).get().await()
                    doc.getString("role") ?: "STUDENT"
                }
            } catch (e: Exception) {
                // If timeout or offline, try getting from cache
                val doc = db.collection("users").document(uid)
                    .get(com.google.firebase.firestore.Source.CACHE)
                    .await()
                doc.getString("role") ?: "STUDENT"
            }

            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser() = auth.currentUser

    suspend fun getUserRole(uid: String): String? {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.getString("role")
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }
}