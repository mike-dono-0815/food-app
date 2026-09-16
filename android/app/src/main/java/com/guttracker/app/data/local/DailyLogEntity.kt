package com.guttracker.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val medicationTakenAt: Long? = null,
    val wellbeingRating: Int? = null,
    val wellbeingLoggedAt: Long? = null,
    val digestionRating: Int? = null,
    val digestionLoggedAt: Long? = null,
    val contextTagId: Int? = null,
    val notes: String? = null,
    val synced: Boolean = false,
)
