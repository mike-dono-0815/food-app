package com.guttracker.app

import android.app.Application
import com.guttracker.app.work.ReminderScheduler
import com.guttracker.app.work.SyncScheduler

class GutTrackerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        SyncScheduler.syncNow(this)
        SyncScheduler.schedulePeriodic(this)
        ReminderScheduler.scheduleAll(this)
    }
}
