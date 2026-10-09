package com.protocolx.inlo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.protocolx.inlo.data.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY postTime DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isProcessed = 0 ORDER BY postTime ASC")
    suspend fun getUnprocessed(): List<NotificationEntity>

    @Query("UPDATE notifications SET isProcessed = 1 WHERE id = :id")
    suspend fun markProcessed(id: String)
}
