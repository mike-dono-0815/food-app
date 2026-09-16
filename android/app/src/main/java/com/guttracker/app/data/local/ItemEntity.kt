package com.guttracker.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val category: String, // "food" | "drink"
    val useCount: Int,
    val lastUsedAt: Long?,
)
