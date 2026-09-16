package com.guttracker.app.work

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val MEDICATION_WORK = "reminder-medication"
    private const val WELLBEING_WORK = "reminder-wellbeing"

    fun scheduleAll(context: Context) {
        ensureNotificationChannel(context)
        schedule(context, MEDICATION_WORK, ReminderWorker.KIND_MEDICATION, targetHour = 12)
        schedule(context, WELLBEING_WORK, ReminderWorker.KIND_WELLBEING, targetHour = 20)
    }

    private fun schedule(context: Context, uniqueName: String, kind: String, targetHour: Int) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis(targetHour), TimeUnit.MILLISECONDS)
            .setInputData(Data.Builder().putString(ReminderWorker.KEY_KIND, kind).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(uniqueName, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private fun initialDelayMillis(targetHour: Int): Long {
        val now = LocalDateTime.now()
        var target = now.withHour(targetHour).withMinute(0).withSecond(0).withNano(0)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return Duration.between(now, target).toMillis()
    }
}
