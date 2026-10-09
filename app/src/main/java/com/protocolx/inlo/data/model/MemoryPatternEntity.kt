package com.protocolx.inlo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "learning_memory")
data class MemoryPatternEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val patternKey: String, // e.g. "sender:manager", "action:office_arrival", "phrase:come_at"
    val positiveEngagements: Int = 0,
    val negativeCorrections: Int = 0,
    val learnedWeight: Float = 1.0f, // Adaptive multiplier
    val lastUpdated: Long = System.currentTimeMillis()
)
