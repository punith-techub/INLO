package com.protocolx.inlo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.protocolx.inlo.data.model.AutomationStatus
import com.protocolx.inlo.data.model.ScheduledAutomation
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(automation: ScheduledAutomation)

    @Query("SELECT * FROM automations ORDER BY createdTimestamp DESC")
    fun getAllAutomations(): Flow<List<ScheduledAutomation>>

    @Query("UPDATE automations SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: AutomationStatus)

    @Query("DELETE FROM automations WHERE id = :id")
    suspend fun delete(id: String)
}
