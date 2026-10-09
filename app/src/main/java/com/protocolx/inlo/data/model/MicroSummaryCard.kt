package com.protocolx.inlo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "micro_summaries")
data class MicroSummaryCard(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val notificationId: String,
    val tier: PriorityTier,
    val catchyHeadline: String,
    val sourceApp: String,
    val sender: String,
    val rawSnippet: String,
    val actionPill: String? = null,
    val targetTime: String? = null,
    val alarmSetTime: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isDismissed: Boolean = false
)
