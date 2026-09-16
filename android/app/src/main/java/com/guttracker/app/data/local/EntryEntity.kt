package com.guttracker.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * localId is the stable identity the UI works with. serverId is null until the
 * first successful sync — edits/deletes before that just mutate this row directly,
 * and the eventual sync creates the entry server-side with whatever the row's
 * current state is. Once serverId is set, edits/deletes talk to the server by id.
 */
@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val serverId: Int? = null,
    val timestamp: Long,
    val type: String, // "food" | "drink"
    val itemId: Int? = null,
    val label: String? = null,
    val synced: Boolean = false,
    val pendingDelete: Boolean = false,
)
