package com.guttracker.app.data.repository

import android.content.Context
import com.guttracker.app.data.local.DailyLogDao
import com.guttracker.app.data.local.DailyLogEntity
import com.guttracker.app.data.local.TagDao
import com.guttracker.app.work.SyncScheduler
import kotlinx.coroutines.flow.Flow

/** SyncWorker pushes a dirty row's full current state — this is a single device, so there's
 *  no concurrent-write conflict to reconcile, and always sending the whole row keeps the
 *  sync path simple (no per-field diffing needed). */
class DailyLogRepository(
    private val appContext: Context,
    private val dailyLogDao: DailyLogDao,
    private val tagDao: TagDao,
) {
    fun observeByDate(date: String): Flow<DailyLogEntity?> = dailyLogDao.observeByDate(date)

    fun observeSince(startDate: String): Flow<List<DailyLogEntity>> = dailyLogDao.observeSince(startDate)

    private suspend fun currentOrDefault(date: String): DailyLogEntity =
        dailyLogDao.getByDate(date) ?: DailyLogEntity(date = date, contextTagId = tagDao.getHomeTagId())

    suspend fun updateMedication(date: String, takenAtMillis: Long?) {
        val current = currentOrDefault(date)
        dailyLogDao.upsert(current.copy(medicationTakenAt = takenAtMillis, synced = false))
        SyncScheduler.syncNow(appContext)
    }

    suspend fun updateWellbeing(date: String, rating: Double) {
        val current = currentOrDefault(date)
        dailyLogDao.upsert(current.copy(wellbeingRating = rating, wellbeingLoggedAt = System.currentTimeMillis(), synced = false))
        SyncScheduler.syncNow(appContext)
    }

    suspend fun updateDigestion(date: String, rating: Double) {
        val current = currentOrDefault(date)
        dailyLogDao.upsert(current.copy(digestionRating = rating, digestionLoggedAt = System.currentTimeMillis(), synced = false))
        SyncScheduler.syncNow(appContext)
    }

    suspend fun updateContextTag(date: String, tagId: Int) {
        val current = currentOrDefault(date)
        dailyLogDao.upsert(current.copy(contextTagId = tagId, synced = false))
        SyncScheduler.syncNow(appContext)
    }

    suspend fun updateNotes(date: String, notes: String?) {
        val current = currentOrDefault(date)
        dailyLogDao.upsert(current.copy(notes = notes, synced = false))
        SyncScheduler.syncNow(appContext)
    }

    /** The Rating screen batches wellbeing/digestion/tag/notes into one save action rather than syncing per tap. */
    suspend fun saveRating(date: String, wellbeingRating: Double?, digestionRating: Double?, contextTagId: Int?, notes: String?) {
        val current = currentOrDefault(date)
        val now = System.currentTimeMillis()
        dailyLogDao.upsert(
            current.copy(
                wellbeingRating = wellbeingRating,
                wellbeingLoggedAt = if (wellbeingRating != null) now else current.wellbeingLoggedAt,
                digestionRating = digestionRating,
                digestionLoggedAt = if (digestionRating != null) now else current.digestionLoggedAt,
                contextTagId = contextTagId ?: current.contextTagId,
                notes = notes,
                synced = false,
            )
        )
        SyncScheduler.syncNow(appContext)
    }
}
