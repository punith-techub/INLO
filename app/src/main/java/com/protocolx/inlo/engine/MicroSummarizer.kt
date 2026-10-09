package com.protocolx.inlo.engine

import com.protocolx.inlo.data.model.PriorityTier

data class MicroSummaryResult(
    val catchyHeadline: String,
    val actionPill: String?,
    val targetTimeString: String?,
    val alarmTimeString: String?
)

object MicroSummarizer {
    fun summarize(
        tier: PriorityTier,
        sender: String,
        text: String,
        schedule: ExtractedSchedule?,
        alarmResult: AlarmCalculationResult?
    ): MicroSummaryResult {
        val snippet = text.replace("\n", " ").trim()
        val truncated = if (snippet.length > 70) snippet.take(67) + "..." else snippet

        return when (tier) {
            PriorityTier.P0_CRITICAL -> {
                if (alarmResult != null && alarmResult.isEarlyShift) {
                    val prepHours = alarmResult.prepMinutes / 60
                    val prepMins = alarmResult.prepMinutes % 60
                    val prepText = if (prepHours > 0) "${prepHours}h prep" else "${prepMins}m prep"

                    val travelHours = alarmResult.commuteMinutes / 60
                    val travelMins = alarmResult.commuteMinutes % 60
                    val travelText = if (travelHours > 0) "${travelHours}h travel" else "${travelMins}m travel"

                    val headline = "🚨 Early Shift: $sender wants you at ${schedule?.targetLocationOrEvent ?: "office"} by ${alarmResult.formattedTargetTime} → ⏰ Auto-set alarm for ${alarmResult.formattedAlarmTime} ($travelText + $prepText)"
                    val pill = "⏰ ${alarmResult.formattedAlarmTime} Alarm | 📅 Added"
                    MicroSummaryResult(headline, pill, alarmResult.formattedTargetTime, alarmResult.formattedAlarmTime)
                } else {
                    val headline = "🚨 Blocker: $sender reported urgent issue: \"$truncated\""
                    val pill = "⚠️ Immediate Response"
                    MicroSummaryResult(headline, pill, null, null)
                }
            }

            PriorityTier.P1_HIGH -> {
                if (schedule != null) {
                    val h = if (schedule.targetHour % 12 == 0) 12 else schedule.targetHour % 12
                    val amPm = if (schedule.targetHour in 12..23) "PM" else "AM"
                    val timeStr = String.format("%02d:%02d %s", h, schedule.targetMinute, amPm)
                    val headline = "⚡ Schedule Update: $sender set ${schedule.targetLocationOrEvent} for $timeStr"
                    val pill = "📅 Added to Calendar"
                    MicroSummaryResult(headline, pill, timeStr, null)
                } else {
                    val headline = "⚡ Deliverable: $sender noted: \"$truncated\""
                    val pill = "📌 Action Item"
                    MicroSummaryResult(headline, pill, null, null)
                }
            }

            PriorityTier.P2_MEDIUM -> {
                val headline = "📌 Mention / Question: $sender asked: \"$truncated\""
                val pill = "👤 Needs Your Input"
                MicroSummaryResult(headline, pill, null, null)
            }

            PriorityTier.P3_LOW -> {
                val headline = "💬 Update: $sender: \"$truncated\""
                val pill = "☕ Low Priority"
                MicroSummaryResult(headline, pill, null, null)
            }
        }
    }
}
