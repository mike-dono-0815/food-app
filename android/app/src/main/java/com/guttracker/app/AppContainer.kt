package com.guttracker.app

import android.content.Context
import com.guttracker.app.data.local.AppDatabase
import com.guttracker.app.data.remote.ApiService
import com.guttracker.app.data.remote.createApiService
import com.guttracker.app.data.repository.CatalogRepository
import com.guttracker.app.data.repository.DailyLogRepository
import com.guttracker.app.data.repository.EntryRepository

class AppContainer(context: Context) {
    private val db = AppDatabase.get(context)
    val itemDao = db.itemDao()
    val tagDao = db.tagDao()
    val entryDao = db.entryDao()
    val dailyLogDao = db.dailyLogDao()

    val api: ApiService = createApiService(BuildConfig.API_BASE_URL, BuildConfig.API_TOKEN)

    val catalogRepository = CatalogRepository(api, itemDao, tagDao)
    val entryRepository = EntryRepository(context.applicationContext, entryDao, itemDao)
    val dailyLogRepository = DailyLogRepository(context.applicationContext, dailyLogDao, tagDao)
}
