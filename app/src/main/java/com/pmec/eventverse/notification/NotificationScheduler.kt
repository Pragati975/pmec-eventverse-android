package com.pmec.eventverse.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.pmec.eventverse.data.model.Event
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    // Assumed average event duration, used to time the feedback-request notification.
    // Adjust here if most events run longer/shorter than 3 hours.
    private const val ASSUMED_EVENT_DURATION_HOURS = 3L

    /**
     * Combines event.date (day) with event.time (e.g. "06:00 PM") into a single timestamp.
     * Falls back to just event.date if event.time can't be parsed.
     */
    private fun eventStartTimeMillis(event: Event): Long {
        return try {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val parsedTime = timeFormat.parse(event.time) ?: return event.date

            val timeCal = Calendar.getInstance().apply { time = parsedTime }
            val dateCal = Calendar.getInstance().apply {
                timeInMillis = event.date
                set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            dateCal.timeInMillis
        } catch (e: Exception) {
            event.date
        }
    }

    fun scheduleEventReminder(context: Context, event: Event) {
        val startMillis = eventStartTimeMillis(event)
        val reminderTimeMillis = startMillis - TimeUnit.DAYS.toMillis(1)
        val delay = reminderTimeMillis - System.currentTimeMillis()

        // Event is less than a day away (or in the past) — skip the reminder, it'd fire immediately or never.
        if (delay <= 0) return

        val inputData = workDataOf(
            EventReminderWorker.KEY_EVENT_TITLE to event.title,
            EventReminderWorker.KEY_EVENT_VENUE to event.venue,
            EventReminderWorker.KEY_EVENT_ID to event.eventId
        )

        val request = OneTimeWorkRequestBuilder<EventReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "reminder_${event.eventId}",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun scheduleFeedbackRequest(context: Context, event: Event) {
        val startMillis = eventStartTimeMillis(event)
        val feedbackTimeMillis = startMillis + TimeUnit.HOURS.toMillis(ASSUMED_EVENT_DURATION_HOURS)
        val delay = feedbackTimeMillis - System.currentTimeMillis()

        if (delay <= 0) return

        val inputData = workDataOf(
            FeedbackRequestWorker.KEY_EVENT_TITLE to event.title,
            FeedbackRequestWorker.KEY_EVENT_ID to event.eventId
        )

        val request = OneTimeWorkRequestBuilder<FeedbackRequestWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "feedback_${event.eventId}",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}