package com.guttracker.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.guttracker.app.GutTrackerApp
import com.guttracker.app.util.todayDateString

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val kind = inputData.getString(KEY_KIND) ?: return Result.failure()
        val container = (applicationContext as GutTrackerApp).container
        val today = container.dailyLogDao.getByDate(todayDateString())

        when (kind) {
            KIND_MEDICATION -> if (today?.medicationTakenAt == null) {
                showReminderNotification(applicationContext, NOTIFICATION_ID_MEDICATION, "Medication", "Have you taken it yet today?")
            }
            KIND_WELLBEING -> if (today?.wellbeingRating == null) {
                showReminderNotification(applicationContext, NOTIFICATION_ID_WELLBEING, "Log today's rating", "Wellbeing & digestion — not done yet")
            }
        }
        return Result.success()
    }

    companion object {
        const val KEY_KIND = "kind"
        const val KIND_MEDICATION = "medication"
        const val KIND_WELLBEING = "wellbeing"
        const val NOTIFICATION_ID_MEDICATION = 1
        const val NOTIFICATION_ID_WELLBEING = 2
    }
}
