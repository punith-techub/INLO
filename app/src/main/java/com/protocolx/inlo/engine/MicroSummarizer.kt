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
        return LocalAiSummarizer.summarizeSingle(tier, sender, text, schedule, alarmResult)
    }
}

