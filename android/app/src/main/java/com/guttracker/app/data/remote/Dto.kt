package com.guttracker.app.data.remote

data class ItemDto(val id: Int, val name: String, val category: String, val useCount: Int, val lastUsedAt: String?)
data class TagDto(val id: Int, val name: String, val useCount: Int, val lastUsedAt: String?)
data class EntryDto(val id: Int, val timestamp: String, val type: String, val itemId: Int?, val label: String?)
data class DailyLogDto(
    val date: String,
    val medicationTakenAt: String?,
    val wellbeingRating: Int?,
    val wellbeingLoggedAt: String?,
    val digestionRating: Int?,
    val digestionLoggedAt: String?,
    val contextTagId: Int?,
    val notes: String?,
)

data class CreateItemRequest(val name: String)
data class CreateTagRequest(val name: String)
data class CreateEntryRequest(val timestamp: String, val type: String, val itemId: Int?, val label: String?)
data class UpdateEntryRequest(val timestamp: String)
