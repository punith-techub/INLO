package com.protocolx.inlo.engine

import android.content.Context
import android.util.Log
import com.protocolx.inlo.automation.CalendarSyncDispatcher
import com.protocolx.inlo.automation.LocalNotificationNotifier
import com.protocolx.inlo.automation.SystemAlarmDispatcher
import com.protocolx.inlo.data.db.AppDatabase
import com.protocolx.inlo.data.model.MicroSummaryCard
import com.protocolx.inlo.data.model.NotificationEntity
import com.protocolx.inlo.data.model.PriorityTier
import com.protocolx.inlo.data.preferences.UserPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationProcessor {
    private const val TAG = "NotificationProcessor"
    private val scope = CoroutineScope(Dispatchers.IO)

    fun process(
        context: Context,
        packageName: String,
        appName: String,
        title: String,
        text: String,
        subText: String? = null,
        postTime: Long = System.currentTimeMillis()
    ) {
        scope.launch {
            try {
                // Step 1: Privacy Shield Verification
                if (PrivacyShieldFilter.isSensitive(packageName, title, text)) {
                    Log.d(TAG, "Shield dropped sensitive notification from $packageName")
                    return@launch
                }

                val db = AppDatabase.getInstance(context)
                val prefsManager = UserPreferencesManager(context)
                val settings = prefsManager.getSettings()

                // Step 2: Persist Notification Entity
                val notificationEntity = NotificationEntity(
                    packageName = packageName,
                    appName = appName,
                    title = title,
                    text = text,
                    subText = subText,
                    postTime = postTime,
                    isProcessed = true
                )
                db.notificationDao().insert(notificationEntity)

                // Step 3: Extract Schedule and Calculate Alarm
                val schedule = TemporalSignalExtractor.extractSchedule(text)
                val alarmResult = if (schedule != null) {
                    CommuteAlarmCalculator.calculate(schedule.targetHour, schedule.targetMinute, settings)
                } else null

                // Step 4: Query Hidden On-Device Learning Memory
                val learnedWeight = AutonomousMemoryEngine.getLearnedWeight(context, title)

                // Step 5: Classify into 4-Tier Hierarchy using Adaptive Learned Weight
                val tier = PriorityClassifier.classify(title, text, schedule, alarmResult, settings, learnedWeight)

                // Step 6: Generate Catchy Micro-Summary Flashcard
                val summaryResult = MicroSummarizer.summarize(tier, title, text, schedule, alarmResult)

                val summaryCard = MicroSummaryCard(
                    notificationId = notificationEntity.id,
                    tier = tier,
                    catchyHeadline = summaryResult.catchyHeadline,
                    sourceApp = appName,
                    sender = title,
                    rawSnippet = text,
                    actionPill = summaryResult.actionPill,
                    targetTime = summaryResult.targetTimeString,
                    alarmSetTime = summaryResult.alarmTimeString,
                    timestamp = postTime
                )
                db.summaryDao().insert(summaryCard)
                Log.d(TAG, "INLO Generated ${tier.name} Flashcard: ${summaryResult.catchyHeadline}")

                // Step 7: Dispatch Autonomous System Actions if P0 Critical Schedule Shift
                if (tier == PriorityTier.P0_CRITICAL && alarmResult != null && alarmResult.isEarlyShift) {
                    if (settings.isAutonomousAlarmEnabled) {
                        SystemAlarmDispatcher.scheduleAlarm(context, alarmResult, title)
                        LocalNotificationNotifier.notifyAlarmSet(context, alarmResult, title)
                    }

                    if (settings.isAutonomousCalendarEnabled && schedule != null) {
                        CalendarSyncDispatcher.scheduleCalendarEvent(context, schedule, title)
                    }
                } else if (tier == PriorityTier.P1_HIGH && schedule != null && settings.isAutonomousCalendarEnabled) {
                    CalendarSyncDispatcher.scheduleCalendarEvent(context, schedule, title)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification: ${e.message}", e)
            }
        }
    }
}
