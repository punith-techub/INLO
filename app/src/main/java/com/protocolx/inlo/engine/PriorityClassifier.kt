package com.protocolx.inlo.engine

import com.protocolx.inlo.data.model.PriorityTier
import com.protocolx.inlo.data.model.UserSettings

object PriorityClassifier {
    private val BLOCKER_KEYWORDS = listOf(
        "urgent", "asap", "emergency", "sev1", "sev-1", "incident", "outage", 
        "down", "crash", "broken", "critical", "blocking"
    )

    private val DECISION_TASK_KEYWORDS = listOf(
        "decided", "agreed", "approved", "rescheduled", "deadline", "demo", 
        "deploy", "action item", "todo", "deliverable", "signed off", "release"
    )

    private val MENTION_INQUIRY_KEYWORDS = listOf(
        "can you", "could you", "please review", "what do you think", "need your input",
        "waiting for you", "assigned to you", "pr#", "pull request"
    )

    fun classify(
        sender: String,
        text: String,
        schedule: ExtractedSchedule?,
        alarmResult: AlarmCalculationResult?,
        settings: UserSettings,
        learnedWeight: Float = 1.0f // Hidden memory engine adaptive multiplier
    ): PriorityTier {
        val lowerText = text.lowercase()
        val lowerSender = sender.lowercase()

        // 1. VIP Check
        val isVipSender = settings.vipContacts.any { lowerSender.contains(it.lowercase()) } ||
                settings.vipKeywords.any { lowerSender.contains(it) || lowerText.contains(it) }

        // Adaptive Memory suppression: if user repeatedly dismissed or cancelled alarms for this sender (learnedWeight < 0.5)
        val isSuppressedByMemory = learnedWeight < 0.5f

        // 2. Critical Check (P0): Early arrival requested OR Urgent system blocker
        if (!isSuppressedByMemory && alarmResult != null && alarmResult.isEarlyShift && 
            (isVipSender || lowerText.contains("office") || lowerText.contains("work"))) {
            return PriorityTier.P0_CRITICAL
        }

        if (BLOCKER_KEYWORDS.any { lowerText.contains(it) } && !isSuppressedByMemory) {
            return PriorityTier.P0_CRITICAL
        }

        // 3. High Check (P1): Decisions, tasks, meetings, or non-early VIP schedule shifts
        if (DECISION_TASK_KEYWORDS.any { lowerText.contains(it) } || (isVipSender && schedule != null)) {
            return PriorityTier.P1_HIGH
        }

        // 4. Medium Check (P2): Direct mentions or unanswered questions
        if (lowerText.contains("?") || lowerText.contains("@") || MENTION_INQUIRY_KEYWORDS.any { lowerText.contains(it) }) {
            return PriorityTier.P2_MEDIUM
        }

        // 5. Ambient / Banter (P3)
        return PriorityTier.P3_LOW
    }
}
