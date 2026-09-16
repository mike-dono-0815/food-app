package com.guttracker.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY useCount DESC, name ASC")
    fun observeAll(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE category = :category ORDER BY useCount DESC, name ASC")
    fun observeByCategory(category: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: Int): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ItemEntity)

    @Query("UPDATE items SET useCount = useCount + 1, lastUsedAt = :timestampMillis WHERE id = :itemId")
    suspend fun bumpUsage(itemId: Int, timestampMillis: Long)
}
