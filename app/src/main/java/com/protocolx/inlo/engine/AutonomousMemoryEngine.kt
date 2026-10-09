package com.protocolx.inlo.engine

import android.content.Context
import android.util.Log
import com.protocolx.inlo.data.db.AppDatabase
import com.protocolx.inlo.data.model.MemoryPatternEntity
import com.protocolx.inlo.data.model.PriorityTier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AutonomousMemoryEngine {
    private const val TAG = "AutonomousMemory"
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Hidden method: records user correction when an alarm is cancelled or dismissed quickly.
     * Exponentially penalizes false-positive triggers.
     */
    fun recordCorrection(context: Context, sender: String, detail: String) {
        scope.launch {
            try {
                val db = AppDatabase.getInstance(context)
                val key = "sender:${sender.lowercase().trim()}"
                val existing = db.memoryDao().getPattern(key)

                val updatedWeight = if (existing != null) {
                    // Exponential penalty: 0.75x multiplier
                    (existing.learnedWeight * 0.75f).coerceAtLeast(0.2f)
                } else {
                    0.75f
                }

                val entity = MemoryPatternEntity(
                    id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                    patternKey = key,
                    positiveEngagements = existing?.positiveEngagements ?: 0,
                    negativeCorrections = (existing?.negativeCorrections ?: 0) + 1,
                    learnedWeight = updatedWeight,
                    lastUpdated = System.currentTimeMillis()
                )
                db.memoryDao().insertOrUpdate(entity)
                Log.d(TAG, "Learned from mistake on [$key]: Weight updated to $updatedWeight")
            } catch (e: Exception) {
                Log.e(TAG, "Memory error: ${e.message}")
            }
        }
    }

    /**
     * Hidden method: records positive engagement when user keeps alarm/task.
     * Exponentially reinforces true-positive triggers.
     */
    fun recordPositive(context: Context, sender: String) {
        scope.launch {
            try {
                val db = AppDatabase.getInstance(context)
                val key = "sender:${sender.lowercase().trim()}"
                val existing = db.memoryDao().getPattern(key)

                val updatedWeight = if (existing != null) {
                    // Exponential reinforcement: 1.25x multiplier
                    (existing.learnedWeight * 1.25f).coerceAtMost(3.0f)
                } else {
                    1.25f
                }

                val entity = MemoryPatternEntity(
                    id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                    patternKey = key,
                    positiveEngagements = (existing?.positiveEngagements ?: 0) + 1,
                    negativeCorrections = existing?.negativeCorrections ?: 0,
                    learnedWeight = updatedWeight,
                    lastUpdated = System.currentTimeMillis()
                )
                db.memoryDao().insertOrUpdate(entity)
                Log.d(TAG, "Reinforced pattern [$key]: Weight updated to $updatedWeight")
            } catch (e: Exception) {
                Log.e(TAG, "Memory error: ${e.message}")
            }
        }
    }

    /**
     * Retrieves the learned adaptive multiplier for a given sender/pattern.
     */
    suspend fun getLearnedWeight(context: Context, sender: String): Float {
        return try {
            val db = AppDatabase.getInstance(context)
            val key = "sender:${sender.lowercase().trim()}"
            val existing = db.memoryDao().getPattern(key)
            existing?.learnedWeight ?: 1.0f
        } catch (e: Exception) {
            1.0f
        }
    }
}
