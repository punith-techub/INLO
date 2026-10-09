package com.protocolx.inlo.automation

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
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

        // Check permission first
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Write calendar permission not granted yet, skipping native calendar insert")
            return
        }

        val targetCal = Calendar.getInstance().apply {
            if (schedule.isTomorrow) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            set(Calendar.HOUR_OF_DAY, schedule.targetHour)
            set(Calendar.MINUTE, schedule.targetMinute)
            set(Calendar.SECOND, 0)
        }

        val endCal = (targetCal.clone() as Calendar).apply {
            add(Calendar.HOUR_OF_DAY, 2)
        }

        val title = "${schedule.targetLocationOrEvent} ($sender)"

        try {
            var calendarId: Long = 1
            // Attempt to query existing calendar ID safely
            try {
                val projection = arrayOf(CalendarContract.Calendars._ID)
                val cursor = appContext.contentResolver.query(
                    CalendarContract.Calendars.CONTENT_URI,
                    projection,
                    CalendarContract.Calendars.VISIBLE + " = 1",
                    null,
                    CalendarContract.Calendars._ID + " ASC"
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        calendarId = it.getLong(0)
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Using default calendar ID: ${e.message}")
            }

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, targetCal.timeInMillis)
                put(CalendarContract.Events.DTEND, endCal.timeInMillis)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, note)
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }

            val uri = appContext.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            Log.d(TAG, "Calendar event inserted: $uri")
        } catch (t: Throwable) {
            Log.e(TAG, "Safe catch: Failed to insert calendar event: ${t.message}")
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
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to record calendar automation: ${t.message}")
        }
    }
}
