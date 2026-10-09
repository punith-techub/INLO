package com.protocolx.inlo.automation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.protocolx.inlo.R
import com.protocolx.inlo.engine.AlarmCalculationResult

object LocalNotificationNotifier {
    private const val CHANNEL_ID = "INLO_alerts"
    private const val CHANNEL_NAME = "INLO Action Alerts"

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
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
        val appContext = context.applicationContext
        ensureChannel(appContext)

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

        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(alarmResult.alarmHour * 100 + alarmResult.alarmMinute, notification)
    }

    fun notifyAlarmFired(context: Context, label: String) {
        val appContext = context.applicationContext
        ensureChannel(appContext)

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⏰ Wake-Up Call!")
            .setContentText(label)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .build()

        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(9999, notification)
    }
}
