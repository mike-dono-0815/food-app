package com.guttracker.app.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.guttracker.app.AppContainer
import com.guttracker.app.GutTrackerApp
import com.guttracker.app.data.local.DailyLogEntity
import com.guttracker.app.data.local.EntryEntity
import com.guttracker.app.data.local.ItemEntity
import com.guttracker.app.data.local.TagEntity
import com.guttracker.app.data.remote.CreateEntryRequest
import com.guttracker.app.data.remote.UpdateEntryRequest
import com.guttracker.app.util.isoToMillis
import com.guttracker.app.util.millisToIso
import com.guttracker.app.util.todayDateString
import java.time.LocalDate

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val c = (applicationContext as GutTrackerApp).container
        return try {
            pushPendingEntries(c)
            pushPendingDailyLogs(c)
            pullCatalog(c)
            pullRecentWindow(c)
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "sync failed", e)
            Result.retry()
        }
    }

    private suspend fun pushPendingEntries(c: AppContainer) {
        for (e in c.entryDao.getPendingSync()) {
            if (e.pendingDelete) {
                if (e.serverId != null) c.api.deleteEntry(e.serverId)
                c.entryDao.deleteByLocalId(e.localId)
            } else if (e.serverId == null) {
                val dto = c.api.createEntry(CreateEntryRequest(millisToIso(e.timestamp), e.type, e.itemId, e.label))
                c.entryDao.markSynced(e.localId, dto.id)
                if (e.itemId != null) c.itemDao.bumpUsage(e.itemId, e.timestamp)
            } else {
                c.api.updateEntry(e.serverId, UpdateEntryRequest(millisToIso(e.timestamp)))
                c.entryDao.markSynced(e.localId, e.serverId)
            }
        }
    }

    private suspend fun pushPendingDailyLogs(c: AppContainer) {
        for (d in c.dailyLogDao.getPendingSync()) {
            val body = buildMap<String, Any?> {
                // medicationTakenAt has no separate "logged at" field, so it's always safe to
                // include — null legitimately means "not taken" / undone. The rating/tag/notes
                // fields are only included when set, since the server stamps a *LoggedAt the
                // moment it sees the key present, and we don't want a phantom timestamp for a
                // rating that's still genuinely unset.
                put("medicationTakenAt", d.medicationTakenAt?.let { millisToIso(it) })
                d.wellbeingRating?.let { put("wellbeingRating", it) }
                d.digestionRating?.let { put("digestionRating", it) }
                d.contextTagId?.let { put("contextTagId", it) }
                d.notes?.let { put("notes", it) }
            }
            c.api.patchDailyLog(d.date, body)
            c.dailyLogDao.markSynced(d.date)
        }
    }

    private suspend fun pullCatalog(c: AppContainer) {
        val items = c.api.getItems()
        c.itemDao.upsertAll(items.map { ItemEntity(it.id, it.name, it.category, it.useCount, isoToMillis(it.lastUsedAt)) })
        val tags = c.api.getTags()
        c.tagDao.upsertAll(tags.map { TagEntity(it.id, it.name, it.useCount, isoToMillis(it.lastUsedAt)) })
    }

    private suspend fun pullRecentWindow(c: AppContainer) {
        val from = LocalDate.now().minusDays(90).toString()
        val to = todayDateString()

        val entries = c.api.getEntriesRange(from, to)
        for (dto in entries) {
            val millis = isoToMillis(dto.timestamp) ?: continue
            c.entryDao.upsertFromServer(
                EntryEntity(
                    serverId = dto.id,
                    timestamp = millis,
                    type = dto.type,
                    itemId = dto.itemId,
                    label = dto.label,
                    synced = true,
                    pendingDelete = false,
                )
            )
        }

        val dailyLogs = c.api.getDailyLogsRange(from, to)
        for (dto in dailyLogs) {
            c.dailyLogDao.upsert(
                DailyLogEntity(
                    date = dto.date,
                    medicationTakenAt = isoToMillis(dto.medicationTakenAt),
                    wellbeingRating = dto.wellbeingRating,
                    wellbeingLoggedAt = isoToMillis(dto.wellbeingLoggedAt),
                    digestionRating = dto.digestionRating,
                    digestionLoggedAt = isoToMillis(dto.digestionLoggedAt),
                    contextTagId = dto.contextTagId,
                    notes = dto.notes,
                    synced = true,
                )
            )
        }
    }
}
