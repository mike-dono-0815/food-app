package com.guttracker.app.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.guttracker.app.R

private const val CHANNEL_ID = "reminders"

fun ensureNotificationChannel(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)
    val channel = NotificationChannel(CHANNEL_ID, "Reminders", NotificationManager.IMPORTANCE_DEFAULT)
    manager.createNotificationChannel(channel)
}

fun showReminderNotification(context: Context, notificationId: Int, title: String, text: String) {
    if (Build.VERSION.SDK_INT >= 33 &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) return

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(text)
        .setAutoCancel(true)
        .build()

    context.getSystemService(NotificationManager::class.java).notify(notificationId, notification)
}
