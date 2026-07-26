package com.pmec.eventverse.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pmec.eventverse.R

object NotificationHelper {

    const val CHANNEL_ID = "eventverse_notifications"
    private const val CHANNEL_NAME = "Event Updates"
    private const val CHANNEL_DESC = "Registration confirmations, reminders, and feedback requests"

    const val NOTIFICATION_ID_REGISTRATION = 1001
    const val NOTIFICATION_ID_REMINDER = 2001
    const val NOTIFICATION_ID_FEEDBACK = 3001

    const val TYPE_REGISTRATION = "REGISTRATION"
    const val TYPE_REMINDER = "REMINDER"
    const val TYPE_FEEDBACK = "FEEDBACK"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Saves a lightweight record so the in-app "Recent Activity" feed can show it later. */
    private fun logToFirestore(title: String, message: String, type: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val record = hashMapOf(
            "title" to title,
            "message" to message,
            "type" to type,
            "timestamp" to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("notifications")
            .add(record)
    }

    fun showRegistrationConfirmed(context: Context, eventTitle: String, notificationId: Int) {
        val title = "Registration Confirmed 🎉"
        val message = "You're registered for $eventTitle!"

        logToFirestore(title, message, TYPE_REGISTRATION)

        if (!hasPermission(context)) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$message We'll remind you a day before it starts.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        androidx.core.app.NotificationManagerCompat.from(context)
            .notify(notificationId, notification)
    }

    fun showEventReminder(context: Context, eventTitle: String, eventVenue: String, notificationId: Int) {
        val title = "Tomorrow: $eventTitle"
        val message = "Don't forget — at $eventVenue tomorrow!"

        logToFirestore(title, message, TYPE_REMINDER)

        if (!hasPermission(context)) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        androidx.core.app.NotificationManagerCompat.from(context)
            .notify(notificationId, notification)
    }

    fun showFeedbackRequest(context: Context, eventTitle: String, notificationId: Int) {
        val title = "How was $eventTitle?"
        val message = "Tap to share your feedback — it only takes a minute!"

        logToFirestore(title, message, TYPE_FEEDBACK)

        if (!hasPermission(context)) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        androidx.core.app.NotificationManagerCompat.from(context)
            .notify(notificationId, notification)
    }
}