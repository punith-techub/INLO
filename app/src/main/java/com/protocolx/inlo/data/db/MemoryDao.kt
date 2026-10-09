package com.protocolx.inlo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.protocolx.inlo.data.model.MemoryPatternEntity

@Dao
interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(pattern: MemoryPatternEntity)

    @Query("SELECT * FROM learning_memory WHERE patternKey = :key LIMIT 1")
    suspend fun getPattern(key: String): MemoryPatternEntity?

    @Query("SELECT * FROM learning_memory")
    suspend fun getAllPatterns(): List<MemoryPatternEntity>
}
