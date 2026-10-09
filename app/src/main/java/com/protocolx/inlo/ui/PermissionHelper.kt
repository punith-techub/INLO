package com.protocolx.inlo.ui

import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

object PermissionHelper {
    fun isNotificationAccessGranted(context: Context): Boolean {
        return try {
            val packageName = context.packageName
            val packages = NotificationManagerCompat.getEnabledListenerPackages(context)
            if (packages.contains(packageName)) {
                return true
            }

            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            if (!flat.isNullOrEmpty()) {
                val names = flat.split(":")
                for (name in names) {
                    val cn = ComponentName.unflattenFromString(name)
                    if (cn != null && cn.packageName == packageName) {
                        return true
                    }
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    fun openNotificationAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (ignored: Exception) {}
        }
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                alarmManager?.canScheduleExactAlarms() ?: false
            } else {
                true
            }
        } catch (e: Exception) {
            true
        }
    }

    fun openExactAlarmSettings(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } catch (ignored: Exception) {}
    }
}
