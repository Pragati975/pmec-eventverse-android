package com.pmec.eventverse.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class EventReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val eventTitle = inputData.getString(KEY_EVENT_TITLE) ?: return Result.failure()
        val eventVenue = inputData.getString(KEY_EVENT_VENUE) ?: ""
        val eventId = inputData.getString(KEY_EVENT_ID) ?: ""

        NotificationHelper.createNotificationChannel(applicationContext)
        NotificationHelper.showEventReminder(
            context = applicationContext,
            eventTitle = eventTitle,
            eventVenue = eventVenue,
            notificationId = NotificationHelper.NOTIFICATION_ID_REMINDER + eventId.hashCode()
        )

        return Result.success()
    }

    companion object {
        const val KEY_EVENT_TITLE = "event_title"
        const val KEY_EVENT_VENUE = "event_venue"
        const val KEY_EVENT_ID = "event_id"
    }
}