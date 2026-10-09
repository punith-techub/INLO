package com.protocolx.inlo.recap

import com.protocolx.inlo.data.model.MicroSummaryCard
import com.protocolx.inlo.data.model.PriorityTier

data class RecapReport(
    val title: String,
    val subtitle: String,
    val totalCount: Int,
    val p0Count: Int,
    val p1Count: Int,
    val p2Count: Int,
    val p3Count: Int,
    val keyTakeaways: List<String>,
    val scheduledActions: List<String>
)

object RecapEngine {
    fun generateCatchUpRecap(cards: List<MicroSummaryCard>, periodName: String = "Instant Catch-Up"): RecapReport {
        val p0Cards = cards.filter { it.tier == PriorityTier.P0_CRITICAL }
        val p1Cards = cards.filter { it.tier == PriorityTier.P1_HIGH }
        val p2Cards = cards.filter { it.tier == PriorityTier.P2_MEDIUM }
        val p3Cards = cards.filter { it.tier == PriorityTier.P3_LOW }

        val takeaways = mutableListOf<String>()
        val actions = mutableListOf<String>()

        p0Cards.forEach { card ->
            takeaways.add("🚨 [CRITICAL] ${card.catchyHeadline}")
            if (card.actionPill != null) {
                actions.add("${card.actionPill} (${card.sender})")
            }
        }

        p1Cards.take(4).forEach { card ->
            takeaways.add("⚡ [HIGH] ${card.catchyHeadline}")
            if (card.actionPill != null) {
                actions.add("${card.actionPill} (${card.sender})")
            }
        }

        p2Cards.take(3).forEach { card ->
            takeaways.add("📌 [MEDIUM] ${card.sender}: ${card.catchyHeadline}")
        }

        if (p3Cards.isNotEmpty()) {
            takeaways.add("💬 [AMBIENT] ${p3Cards.size} low-priority updates grouped from ${p3Cards.map { it.sourceApp }.distinct().joinToString(", ")}")
        }

        if (takeaways.isEmpty()) {
            takeaways.add("✨ All caught up! No unread notifications or pending priority alerts.")
        }

        return RecapReport(
            title = periodName,
            subtitle = "${cards.size} total items processed 100% on-device",
            totalCount = cards.size,
            p0Count = p0Cards.size,
            p1Count = p1Cards.size,
            p2Count = p2Cards.size,
            p3Count = p3Cards.size,
            keyTakeaways = takeaways,
            scheduledActions = actions
        )
    }
}
