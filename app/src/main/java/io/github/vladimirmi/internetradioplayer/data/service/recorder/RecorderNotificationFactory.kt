package io.github.vladimirmi.internetradioplayer.data.service.recorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.Build
import androidx.core.app.NotificationCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.service.CHANNEL_ID
import io.github.vladimirmi.internetradioplayer.extensions.notificationManager

/**
 * Created by Vladimir Mikhalev 09.02.2019.
 */

class RecorderNotificationFactory(private val service: Service) {

    private val notificationManager = service.notificationManager

    init {
        createNotificationChannel()
    }

    fun createAndStart(id: Int, name: String, isForeground: Boolean): RecorderNotification {
        val notification = createNotification(name)
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

    private fun createNotification(name: String): Notification {
        return NotificationCompat.Builder(service, CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_station_1)
                .setContentTitle(name)
                .setContentText("Recording...")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setUsesChronometer(true)
                .build()
    }

    private fun createNotificationChannel() {
        //todo Refactor duplicate code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = service.getString(R.string.notification_name)
            val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW)

            channel.description = service.getString(R.string.notification_name)
            notificationManager.createNotificationChannel(channel)
        }
    }
}

data class RecorderNotification(val id: Int, val notification: Notification, val isForeground: Boolean)