package com.guttracker.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries WHERE timestamp >= :startMillis AND timestamp < :endMillis AND pendingDelete = 0 ORDER BY timestamp ASC")
    fun observeForRange(startMillis: Long, endMillis: Long): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE localId = :localId")
    suspend fun getByLocalId(localId: Long): EntryEntity?

    @Query("SELECT * FROM entries WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Int): EntryEntity?

    @Query("SELECT * FROM entries WHERE synced = 0")
    suspend fun getPendingSync(): List<EntryEntity>

    @Insert
    suspend fun insert(entry: EntryEntity): Long

    @Update
    suspend fun update(entry: EntryEntity)

    @Query("DELETE FROM entries WHERE localId = :localId")
    suspend fun deleteByLocalId(localId: Long)

    @Query("UPDATE entries SET serverId = :serverId, synced = 1 WHERE localId = :localId")
    suspend fun markSynced(localId: Long, serverId: Int)

    /** Upserts a row pulled from the server, keyed by serverId, preserving the local row's localId if it already exists. */
    @Transaction
    suspend fun upsertFromServer(entry: EntryEntity) {
        val existing = entry.serverId?.let { getByServerId(it) }
        if (existing != null) {
            update(entry.copy(localId = existing.localId))
        } else {
            insert(entry)
        }
    }
}
