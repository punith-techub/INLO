package com.protocolx.inlo.automation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import android.util.Log
import com.protocolx.inlo.data.db.AppDatabase
import com.protocolx.inlo.data.model.AutomationStatus
import com.protocolx.inlo.data.model.AutomationType
import com.protocolx.inlo.data.model.ScheduledAutomation
import com.protocolx.inlo.engine.AlarmCalculationResult
import java.util.Calendar

object SystemAlarmDispatcher {
    private const val TAG = "SystemAlarmDispatcher"

    suspend fun scheduleAlarm(
        context: Context,
        alarmResult: AlarmCalculationResult,
        sender: String,
        label: String = "Early Office Arrival ($sender)"
    ) {
        val appContext = context.applicationContext

        // 1. Dispatch Alarm via AlarmClock System Intent
        try {
            val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, alarmResult.alarmHour)
                putExtra(AlarmClock.EXTRA_MINUTES, alarmResult.alarmMinute)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (alarmIntent.resolveActivity(appContext.packageManager) != null) {
                appContext.startActivity(alarmIntent)
                Log.d(TAG, "Successfully fired ACTION_SET_ALARM for ${alarmResult.formattedAlarmTime}")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Safe catch: launching AlarmClock intent: ${t.message}")
        }

        // 2. Schedule precise fallback using AlarmManager
        try {
            val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager != null) {
                val calendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1) // Tomorrow
                    set(Calendar.HOUR_OF_DAY, alarmResult.alarmHour)
                    set(Calendar.MINUTE, alarmResult.alarmMinute)
                    set(Calendar.SECOND, 0)
                }

                val intent = Intent(appContext, AlarmReceiver::class.java).apply {
                    putExtra("EXTRA_LABEL", label)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    appContext,
                    alarmResult.alarmHour * 100 + alarmResult.alarmMinute,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Safe catch: AlarmManager scheduling: ${t.message}")
        }

        // 3. Persist record in local database
        try {
            val targetCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, alarmResult.alarmHour)
                set(Calendar.MINUTE, alarmResult.alarmMinute)
            }
            val record = ScheduledAutomation(
                type = AutomationType.ALARM,
                title = label,
                detail = "Auto-set wake-up for ${alarmResult.formattedTargetTime} arrival (${alarmResult.commuteMinutes/60}h travel + ${alarmResult.prepMinutes/60}h prep)",
                scheduledTimeDisplay = alarmResult.formattedAlarmTime,
                targetTimestamp = targetCal.timeInMillis,
                status = AutomationStatus.ACTIVE
            )
            AppDatabase.getInstance(appContext).automationDao().insert(record)
        } catch (t: Throwable) {
            Log.e(TAG, "Safe catch: Persisting automation record: ${t.message}")
        }
    }
}
