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

class RecorderNotification(private val service: Service) {

    companion object {
        private const val NOTIFICATION_ID = 74
    }

    private val notificationManager = service.notificationManager

    init {
        createNotificationChannel()
    }

    fun start() {
        service.startForeground(NOTIFICATION_ID, createNotification())
    }

    fun stop() {
        service.stopForeground(true)
        service.stopSelf()
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(service, CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_station_1)
                .setContentTitle("Recording...")
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