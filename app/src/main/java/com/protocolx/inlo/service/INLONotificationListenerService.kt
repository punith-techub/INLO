package com.protocolx.inlo.service

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.protocolx.inlo.engine.NotificationProcessor

class INLONotificationListenerService : NotificationListenerService() {

    private val TAG = "INLONotificationListener"

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "INLO NotificationListener connected and active")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        try {
            val packageName = sbn.packageName ?: return

            // Ignore notifications posted by INLO itself to avoid loops
            if (packageName == applicationContext.packageName) {
                return
            }

            val notification = sbn.notification ?: return
            val extras = notification.extras ?: return

            val title = extras.getString(Notification.EXTRA_TITLE)
                ?: extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                ?: ""

            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
                ?: ""

            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

            // Ignore empty or non-informational notifications
            if (title.isBlank() && text.isBlank()) {
                return
            }

            val appName = try {
                val pm = packageManager
                val appInfo = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                packageName.substringAfterLast(".")
            }

            Log.d(TAG, "Intercepted notification from $appName: Title='$title', Text='$text'")

            NotificationProcessor.process(
                context = applicationContext,
                packageName = packageName,
                appName = appName,
                title = title,
                text = text,
                subText = subText,
                postTime = sbn.postTime
            )
        } catch (t: Throwable) {
            Log.e(TAG, "Safe catch: Error handling notification: ${t.message}", t)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
