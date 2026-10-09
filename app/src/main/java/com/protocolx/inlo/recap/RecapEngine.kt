package com.protocolx.inlo.recap

import com.protocolx.inlo.data.model.MicroSummaryCard
import com.protocolx.inlo.data.model.PriorityTier
import com.protocolx.inlo.engine.LocalAiSummarizer

data class RecapReport(
    val title: String,
    val subtitle: String,
    val totalCount: Int,
    val p0Count: Int,
    val p1Count: Int,
    val p2Count: Int,
    val p3Count: Int,
    val executiveBrief: String = "",
    val keyTakeaways: List<String>,
    val scheduledActions: List<String>
)

object RecapEngine {
    fun generateCatchUpRecap(cards: List<MicroSummaryCard>, periodName: String = "Instant Catch-Up"): RecapReport {
        val aiGlobal = LocalAiSummarizer.summarizeAll(cards)

        val actions = mutableListOf<String>()
        cards.forEach { card ->
            if (card.tier == PriorityTier.P0_CRITICAL || card.tier == PriorityTier.P1_HIGH) {
                if (card.actionPill != null && !card.actionPill.contains("Ambient", ignoreCase = true)) {
                    actions.add("${card.actionPill} (${card.sender})")
                }
            }
        }

        val takeaways = mutableListOf<String>()
        takeaways.addAll(aiGlobal.criticalAlerts)
        takeaways.addAll(aiGlobal.highPriorityTasks)
        takeaways.addAll(aiGlobal.mentionsAndInquiries)
        if (aiGlobal.ambientDigest != null) {
            takeaways.add(aiGlobal.ambientDigest)
        }

        if (takeaways.isEmpty()) {
            takeaways.add("✨ All caught up! No unread notifications or pending priority alerts.")
        }

        return RecapReport(
            title = periodName,
            subtitle = "${cards.size} total items processed 100% on-device via Local AI",
            totalCount = cards.size,
            p0Count = aiGlobal.p0Count,
            p1Count = aiGlobal.p1Count,
            p2Count = aiGlobal.p2Count,
            p3Count = aiGlobal.p3Count,
            executiveBrief = aiGlobal.executiveBrief,
            keyTakeaways = takeaways,
            scheduledActions = actions.distinct()
        )
    }
}

