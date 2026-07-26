package com.pmec.eventverse.util

import com.pmec.eventverse.data.model.Event
import java.text.SimpleDateFormat
import java.util.*

object EventTimeUtils {

    /**
     * Combines event.date (day) with event.time (e.g. "06:00 PM") into a single timestamp.
     * Falls back to just event.date if event.time can't be parsed.
     */
    fun eventStartTimeMillis(event: Event): Long {
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

    /** True if the event's start date/time is in the past relative to now. */
    fun isEventPast(event: Event): Boolean {
        return eventStartTimeMillis(event) < System.currentTimeMillis()
    }
}