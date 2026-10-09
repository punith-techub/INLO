package com.protocolx.inlo.automation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.protocolx.inlo.engine.AlarmCalculationResult

object LocalNotificationNotifier {
    private const val CHANNEL_ID = "inlo_alerts"
    private const val CHANNEL_NAME = "INLO Action Alerts"

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts for auto-scheduled alarms and priority events"
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun notifyAlarmSet(context: Context, alarmResult: AlarmCalculationResult, sender: String) {
        try {
            val appContext = context.applicationContext
            ensureChannel(appContext)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.w("LocalNotificationNotifier", "POST_NOTIFICATIONS permission not granted")
                    return
                }
            }

            val travelHours = alarmResult.commuteMinutes / 60
            val prepHours = alarmResult.prepMinutes / 60
            val bufferText = "${travelHours}h travel + ${prepHours}h prep"

            val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("⏰ Alarm Auto-Set: ${alarmResult.formattedAlarmTime}")
                .setContentText("$sender requested ${alarmResult.formattedTargetTime} arrival ($bufferText buffer applied)")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("⏰ Your wake-up alarm is set for ${alarmResult.formattedAlarmTime}.\n\n$sender requested an early arrival by ${alarmResult.formattedTargetTime}. INLO automatically factored in your $bufferText morning buffer.")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify(alarmResult.alarmHour * 100 + alarmResult.alarmMinute, notification)
        } catch (t: Throwable) {
            Log.e("LocalNotificationNotifier", "Safe catch in notifyAlarmSet: ${t.message}")
        }
    }

    fun notifyAlarmFired(context: Context, label: String) {
        try {
            val appContext = context.applicationContext
            ensureChannel(appContext)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }

            val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("⏰ Wake-Up Call!")
                .setContentText(label)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .build()

            val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify(9999, notification)
        } catch (t: Throwable) {
            Log.e("LocalNotificationNotifier", "Safe catch in notifyAlarmFired: ${t.message}")
        }
    }
}
