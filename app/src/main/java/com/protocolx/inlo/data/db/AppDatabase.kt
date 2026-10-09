package com.protocolx.inlo.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.protocolx.inlo.data.model.AutomationStatus
import com.protocolx.inlo.data.model.AutomationType
import com.protocolx.inlo.data.model.MemoryPatternEntity
import com.protocolx.inlo.data.model.MicroSummaryCard
import com.protocolx.inlo.data.model.NotificationEntity
import com.protocolx.inlo.data.model.PriorityTier
import com.protocolx.inlo.data.model.ScheduledAutomation

class Converters {
    @TypeConverter
    fun fromPriorityTier(tier: PriorityTier): String = tier.name

    @TypeConverter
    fun toPriorityTier(value: String): PriorityTier = try {
        PriorityTier.valueOf(value)
    } catch (e: Exception) {
        PriorityTier.P3_LOW
    }

    @TypeConverter
    fun fromAutomationType(type: AutomationType): String = type.name

    @TypeConverter
    fun toAutomationType(value: String): AutomationType = try {
        AutomationType.valueOf(value)
    } catch (e: Exception) {
        AutomationType.ALARM
    }

    @TypeConverter
    fun fromAutomationStatus(status: AutomationStatus): String = status.name

    @TypeConverter
    fun toAutomationStatus(value: String): AutomationStatus = try {
        AutomationStatus.valueOf(value)
    } catch (e: Exception) {
        AutomationStatus.ACTIVE
    }
}

@Database(
    entities = [
        NotificationEntity::class,
        MicroSummaryCard::class,
        ScheduledAutomation::class,
        MemoryPatternEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun summaryDao(): SummaryDao
    abstract fun automationDao(): AutomationDao
    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "INLO_local.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
