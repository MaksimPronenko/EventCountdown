package pronenko.eventcountdown.domain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import pronenko.eventcountdown.R

class NotificationWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val id = inputData.getInt("id", 0)
        val title = inputData.getString("title")
        val message = inputData.getString("message")
        if (title != null && message != null) {
            sendNotification(
                context,
                title = title,
                message = message,
                notificationId = id
            )
        }
        return Result.success()
    }

    private fun sendNotification(context: Context, title: String, message: String, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "reminder_channel"
        val channel =
            NotificationChannel(channelId, "Напоминания", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.event_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

}