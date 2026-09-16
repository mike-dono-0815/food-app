package com.guttracker.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ItemEntity::class, TagEntity::class, EntryEntity::class, DailyLogEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun tagDao(): TagDao
    abstract fun entryDao(): EntryDao
    abstract fun dailyLogDao(): DailyLogDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "gut-tracker.db")
                .build()
                .also { instance = it }
        }
    }
}
