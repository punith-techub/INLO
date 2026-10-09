package com.protocolx.inlo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.protocolx.inlo.data.model.MicroSummaryCard
import com.protocolx.inlo.data.model.PriorityTier
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: MicroSummaryCard)

    @Query("SELECT * FROM micro_summaries WHERE isDismissed = 0 ORDER BY timestamp DESC")
    fun getAllActiveSummaries(): Flow<List<MicroSummaryCard>>

    @Query("SELECT * FROM micro_summaries WHERE tier = :tier AND isDismissed = 0 ORDER BY timestamp DESC")
    fun getSummariesByTier(tier: PriorityTier): Flow<List<MicroSummaryCard>>

    @Query("UPDATE micro_summaries SET isDismissed = 1 WHERE id = :id")
    suspend fun dismissSummary(id: String)

    @Query("DELETE FROM micro_summaries")
    suspend fun clearAll()
}
