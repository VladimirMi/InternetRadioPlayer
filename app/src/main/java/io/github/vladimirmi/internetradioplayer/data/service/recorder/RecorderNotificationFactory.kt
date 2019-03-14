package io.github.vladimirmi.internetradioplayer.data.service.recorder

import android.app.*
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.notificationManager

/**
 * Created by Vladimir Mikhalev 09.02.2019.
 */

class RecorderNotificationFactory(private val service: Service) {

    companion object {
        const val CHANNEL_ID = "internet_radio_recorder_channel"
    }

    private val notificationManager = service.notificationManager

    init {
        createNotificationChannel()
    }

    fun createAndStart(id: Int, uri: Uri, name: String, isForeground: Boolean): RecorderNotification {
        val notification = createNotification(uri, name)
        if (isForeground) {
            service.startForeground(id, notification)
        } else {
            notificationManager.notify(id, notification)
        }
        return RecorderNotification(id, notification, isForeground)
    }

    fun startForeground(notification: RecorderNotification) {
        service.startForeground(notification.id, notification.notification)
    }

    fun cancel(notification: RecorderNotification) {
        notificationManager.cancel(notification.id)
    }

    private fun createNotification(uri: Uri, name: String): Notification {
        //todo to strings
        val stopPendingIntent = stopPendingIntent(uri)
        return NotificationCompat.Builder(service, CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_station_1)
                .setContentTitle(name)
                .setContentText("Recording...")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setUsesChronometer(true)
                .setDeleteIntent(stopPendingIntent)
                .addAction(0, "Stop", stopPendingIntent)
                .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = service.getString(R.string.recorder_channel_name)
            val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH)

            channel.description = service.getString(R.string.player_channel_name)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun stopPendingIntent(uri: Uri): PendingIntent {
        val intent = Intent(service, RecorderService::class.java).apply {
            putExtra(RecorderService.EXTRA_STOP_RECORD, "")
            data = uri
        }
        return PendingIntent.getService(service, 0, intent, 0)
    }
}

data class RecorderNotification(val id: Int, val notification: Notification, val isForeground: Boolean)