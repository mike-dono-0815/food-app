package com.guttracker.app.data.repository

import com.guttracker.app.data.local.ItemDao
import com.guttracker.app.data.local.ItemEntity
import com.guttracker.app.data.local.TagDao
import com.guttracker.app.data.local.TagEntity
import com.guttracker.app.data.remote.ApiService
import com.guttracker.app.data.remote.CreateItemRequest
import com.guttracker.app.data.remote.CreateTagRequest
import com.guttracker.app.util.isoToMillis
import kotlinx.coroutines.flow.Flow

/** Items and tags are read-through caches. Creating one requires connectivity — it's a rare, deliberate action, not the fast-tap path. */
class CatalogRepository(
    private val api: ApiService,
    private val itemDao: ItemDao,
    private val tagDao: TagDao,
) {
    fun observeItems(): Flow<List<ItemEntity>> = itemDao.observeAll()
    fun observeItemsByCategory(category: String): Flow<List<ItemEntity>> = itemDao.observeByCategory(category)
    fun observeTags(): Flow<List<TagEntity>> = tagDao.observeAll()

    suspend fun refresh() {
        val items = api.getItems()
        itemDao.upsertAll(items.map { ItemEntity(it.id, it.name, it.category, it.useCount, isoToMillis(it.lastUsedAt)) })
        val tags = api.getTags()
        tagDao.upsertAll(tags.map { TagEntity(it.id, it.name, it.useCount, isoToMillis(it.lastUsedAt)) })
    }

    suspend fun createFoodItem(name: String): Result<ItemEntity> = runCatching {
        val dto = api.createItem(CreateItemRequest(name))
        val entity = ItemEntity(dto.id, dto.name, dto.category, dto.useCount, isoToMillis(dto.lastUsedAt))
        itemDao.upsert(entity)
        entity
    }

    suspend fun createTag(name: String): Result<TagEntity> = runCatching {
        val dto = api.createTag(CreateTagRequest(name))
        val entity = TagEntity(dto.id, dto.name, dto.useCount, isoToMillis(dto.lastUsedAt))
        tagDao.upsert(entity)
        entity
    }
}
