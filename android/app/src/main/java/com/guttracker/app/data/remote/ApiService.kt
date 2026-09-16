package com.guttracker.app.data.remote

import retrofit2.http.*

interface ApiService {
    @GET("api/items")
    suspend fun getItems(): List<ItemDto>

    @POST("api/items")
    suspend fun createItem(@Body body: CreateItemRequest): ItemDto

    @GET("api/tags")
    suspend fun getTags(): List<TagDto>

    @POST("api/tags")
    suspend fun createTag(@Body body: CreateTagRequest): TagDto

    @GET("api/entries")
    suspend fun getEntries(@Query("date") date: String): List<EntryDto>

    @GET("api/entries")
    suspend fun getEntriesRange(@Query("from") from: String, @Query("to") to: String): List<EntryDto>

    @POST("api/entries")
    suspend fun createEntry(@Body body: CreateEntryRequest): EntryDto

    @PATCH("api/entries/{id}")
    suspend fun updateEntry(@Path("id") id: Int, @Body body: UpdateEntryRequest)

    @DELETE("api/entries/{id}")
    suspend fun deleteEntry(@Path("id") id: Int)

    @GET("api/daily-logs/{date}")
    suspend fun getDailyLog(@Path("date") date: String): DailyLogDto

    @GET("api/daily-logs")
    suspend fun getDailyLogsRange(@Query("from") from: String, @Query("to") to: String): List<DailyLogDto>

    @PATCH("api/daily-logs/{date}")
    suspend fun patchDailyLog(@Path("date") date: String, @Body updates: Map<String, @JvmSuppressWildcards Any?>)
}
