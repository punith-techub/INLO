package com.protocolx.inlo.automation

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import com.protocolx.inlo.data.db.AppDatabase
import com.protocolx.inlo.data.model.AutomationStatus
import com.protocolx.inlo.data.model.AutomationType
import com.protocolx.inlo.data.model.ScheduledAutomation
import com.protocolx.inlo.engine.ExtractedSchedule
import java.util.Calendar
import java.util.TimeZone

object CalendarSyncDispatcher {
    private const val TAG = "CalendarSyncDispatcher"

    suspend fun scheduleCalendarEvent(
        context: Context,
        schedule: ExtractedSchedule,
        sender: String,
        note: String = "Requested by $sender"
    ) {
        val appContext = context.applicationContext

        val targetCal = Calendar.getInstance().apply {
            if (schedule.isTomorrow) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            set(Calendar.HOUR_OF_DAY, schedule.targetHour)
            set(Calendar.MINUTE, schedule.targetMinute)
            set(Calendar.SECOND, 0)
        }

        val endCal = (targetCal.clone() as Calendar).apply {
            add(Calendar.HOUR_OF_DAY, 2) // Default 2 hour duration
        }

        val title = "${schedule.targetLocationOrEvent} ($sender)"

        try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, targetCal.timeInMillis)
                put(CalendarContract.Events.DTEND, endCal.timeInMillis)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, note)
                put(CalendarContract.Events.CALENDAR_ID, 1) // Primary calendar
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }

            val uri = appContext.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            Log.d(TAG, "Calendar event inserted: $uri")
        } catch (e: SecurityException) {
            Log.w(TAG, "Calendar permission not granted yet: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert calendar event: ${e.message}", e)
        }

        // Persist in local database record
        try {
            val record = ScheduledAutomation(
                type = AutomationType.CALENDAR_EVENT,
                title = title,
                detail = note,
                scheduledTimeDisplay = String.format("%02d:%02d", schedule.targetHour, schedule.targetMinute),
                targetTimestamp = targetCal.timeInMillis,
                status = AutomationStatus.ACTIVE
            )
            AppDatabase.getInstance(appContext).automationDao().insert(record)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record calendar automation: ${e.message}")
        }
    }
}
