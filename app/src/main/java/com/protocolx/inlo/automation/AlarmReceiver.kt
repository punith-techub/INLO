package com.protocolx.inlo.automation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val label = intent?.getStringExtra("EXTRA_LABEL") ?: "INLO Wake-Up Alarm"
        Log.d("AlarmReceiver", "Exact wake-up alarm fired: $label")

        LocalNotificationNotifier.notifyAlarmFired(context, label)

        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, alertUri)
            ringtone?.play()
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to play alarm tone: ${e.message}")
        }
    }
}
