package com.guttracker.app.data.repository

import android.content.Context
import com.guttracker.app.data.local.EntryDao
import com.guttracker.app.data.local.EntryEntity
import com.guttracker.app.data.local.ItemDao
import com.guttracker.app.work.SyncScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

class EntryRepository(
    private val appContext: Context,
    private val entryDao: EntryDao,
    private val itemDao: ItemDao,
) {
    fun observeForDate(date: LocalDate): Flow<List<EntryEntity>> {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return entryDao.observeForRange(start, end)
    }

    suspend fun createEntry(timestampMillis: Long, type: String, itemId: Int?, label: String?) {
        entryDao.insert(EntryEntity(timestamp = timestampMillis, type = type, itemId = itemId, label = label))
        if (itemId != null) {
            itemDao.bumpUsage(itemId, timestampMillis)
        }
        SyncScheduler.syncNow(appContext)
    }

    suspend fun editEntry(localId: Long, newTimestampMillis: Long) {
        val existing = entryDao.getByLocalId(localId) ?: return
        entryDao.update(existing.copy(timestamp = newTimestampMillis, synced = false))
        SyncScheduler.syncNow(appContext)
    }

    suspend fun deleteEntry(localId: Long) {
        val existing = entryDao.getByLocalId(localId) ?: return
        if (existing.serverId == null) {
            entryDao.deleteByLocalId(localId)
        } else {
            entryDao.update(existing.copy(pendingDelete = true, synced = false))
            SyncScheduler.syncNow(appContext)
        }
    }
}
