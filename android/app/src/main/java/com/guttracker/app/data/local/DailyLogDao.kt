package com.guttracker.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<DailyLogEntity?>

    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyLogEntity?

    @Query("SELECT * FROM daily_logs WHERE synced = 0")
    suspend fun getPendingSync(): List<DailyLogEntity>

    @Query("SELECT * FROM daily_logs WHERE date >= :startDate ORDER BY date ASC")
    fun observeSince(startDate: String): Flow<List<DailyLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: DailyLogEntity)

    @Query("UPDATE daily_logs SET synced = 1 WHERE date = :date")
    suspend fun markSynced(date: String)
}
