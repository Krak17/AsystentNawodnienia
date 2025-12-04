package com.example.asystentnawodnienia.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.asystentnawodnienia.MainActivity
import java.util.concurrent.TimeUnit

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "water_reminder_channel"
        const val NOTIFICATION_ID = 1
        const val WORK_NAME = "waterReminderWork"
        private val GRACE_PERIOD_MINUTES = 1L
    }

    override fun doWork(): Result {
        val enqueueTime = inputData.getLong("ENQUEUE_TIME", 0)
        val currentTime = System.currentTimeMillis()
        val timeSinceEnqueued = currentTime - enqueueTime

        if (enqueueTime > 0 && timeSinceEnqueued < TimeUnit.MINUTES.toMillis(GRACE_PERIOD_MINUTES)) {
            return Result.success()
        }

        sendNotification(applicationContext)
        return Result.success()
    }

    private fun sendNotification(context: Context) {
        createNotificationChannel(context)

        // Tworzymy intencję, która otworzy MainActivity po kliknięciu powiadomienia
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Czas na wodę!")
            .setContentText("Nie zapomnij wypić szklanki wody, aby pozostać nawodnionym.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Ustawiamy akcję po kliknięciu
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Powiadomienia o nawodnieniu"
            val descriptionText = "Kanał dla przypomnień o piciu wody."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
