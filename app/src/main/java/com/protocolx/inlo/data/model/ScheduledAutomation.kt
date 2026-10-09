package com.protocolx.inlo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class AutomationType {
    ALARM,
    CALENDAR_EVENT
}

enum class AutomationStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}

@Entity(tableName = "automations")
data class ScheduledAutomation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: AutomationType,
    val title: String,
    val detail: String,
    val scheduledTimeDisplay: String,
    val targetTimestamp: Long,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val status: AutomationStatus = AutomationStatus.ACTIVE
)
