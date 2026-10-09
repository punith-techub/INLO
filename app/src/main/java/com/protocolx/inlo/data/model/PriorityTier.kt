package com.protocolx.inlo.data.model

enum class PriorityTier(
    val title: String,
    val emoji: String,
    val colorHex: Long,
    val description: String
) {
    P0_CRITICAL(
        title = "Critical Action",
        emoji = "🚨",
        colorHex = 0xFFEF4444, // Red
        description = "Manager directives, early arrival shifts, urgent blockers"
    ),
    P1_HIGH(
        title = "Decisions & Tasks",
        emoji = "⚡",
        colorHex = 0xFFF59E0B, // Amber/Orange
        description = "Agreed consensus, tasks due soon, meeting updates"
    ),
    P2_MEDIUM(
        title = "Mentions & Inquiries",
        emoji = "📌",
        colorHex = 0xFF10B981, // Teal/Green
        description = "Direct @mentions, pending questions waiting on you"
    ),
    P3_LOW(
        title = "Ambient & Banter",
        emoji = "💬",
        colorHex = 0xFF64748B, // Slate/Gray
        description = "Casual chatter, social rollups, non-urgent newsletters"
    )
}
