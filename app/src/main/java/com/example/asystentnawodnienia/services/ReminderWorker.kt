package com.example.asystentnawodnienia.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "water_reminder_channel"
        const val NOTIFICATION_ID = 1
        const val WORK_NAME = "waterReminderWork"
        // Okres karencji w minutach. Jeśli zadanie uruchomi się w tym czasie, zostanie zignorowane.
        private val GRACE_PERIOD_MINUTES = 1L
    }

    override fun doWork(): Result {
        // Odczytaj czas zaplanowania zadania
        val enqueueTime = inputData.getLong("ENQUEUE_TIME", 0)
        val currentTime = System.currentTimeMillis()

        // Oblicz, ile czasu minęło
        val timeSinceEnqueued = currentTime - enqueueTime

        // Sprawdź, czy czas od zaplanowania jest krótszy niż okres karencji
        if (enqueueTime > 0 && timeSinceEnqueued < TimeUnit.MINUTES.toMillis(GRACE_PERIOD_MINUTES)) {
            // Jeśli tak, zakończ po cichu. To prawdopodobnie tylko start aplikacji.
            return Result.success()
        }

        // Jeśli minęło więcej czasu, wyślij powiadomienie
        sendNotification(applicationContext)
        return Result.success()
    }

    private fun sendNotification(context: Context) {
        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Czas na wodę!")
            .setContentText("Nie zapomnij wypić szklanki wody, aby pozostać nawodnionym.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
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
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
