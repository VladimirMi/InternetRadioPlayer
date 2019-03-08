package io.github.vladimirmi.internetradioplayer.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.service.extensions.PlayerActions
import io.github.vladimirmi.internetradioplayer.extensions.notificationManager

/**
 * Created by Vladimir Mikhalev 20.10.2017.
 */
const val CHANNEL_ID = "internet_radio_player_channel"

class MediaNotification(private val service: PlayerService,
                        private val session: MediaSessionCompat) {

    companion object {
        private const val NOTIFICATION_ID = 73
    }

    private val notificationManager = service.notificationManager

    private val playPauseIntent = PlayerActions.playPauseIntent(service.applicationContext)
    private val stopIntent = PlayerActions.stopIntent(service.applicationContext)
    private val nextIntent = PlayerActions.nextIntent(service.applicationContext)
    private val previousIntent = PlayerActions.previousIntent(service.applicationContext)

    private val mediaStyle = MediaStyle()
            .setMediaSession(session.sessionToken)
            .setShowCancelButton(true)
            .setShowActionsInCompactView(0, 1, 2)
            .setCancelButtonIntent(stopIntent)

    private var isActive = false

    init {
        createNotificationChannel()
    }

    fun update() {
        val state = session.controller.playbackState.state
        when (state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                service.startForeground(NOTIFICATION_ID, createNotification())
                isActive = true
            }
            PlaybackStateCompat.STATE_STOPPED -> {
                service.stopForeground(true)
                isActive = false
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                service.stopForeground(false)
                if (isActive) notificationManager.notify(NOTIFICATION_ID, createNotification())
            }
            else -> notificationManager.notify(NOTIFICATION_ID, createNotification())
        }
    }

    private fun createNotification(): Notification {
        val builder = createBuilder()
        val metadata: MediaMetadataCompat? = session.controller.metadata
        val playbackState = session.controller.playbackState.state

        metadata?.apply {
            builder.setContentTitle(description.title)
                    .setContentText(description.subtitle)
                    .setSubText(description.description)
        }

        if (playbackState == PlaybackStateCompat.STATE_BUFFERING) {
            builder.setContentTitle(service.getString(R.string.metadata_buffering))
        }

        builder.addAction(generateAction(R.drawable.ic_skip_previous, "Previous", previousIntent))
        if (playbackState == PlaybackStateCompat.STATE_STOPPED || playbackState == PlaybackStateCompat.STATE_PAUSED) {
            builder.addAction(generateAction(R.drawable.ic_play, "Play", playPauseIntent))
        } else {
            builder.addAction(generateAction(R.drawable.ic_pause, "Pause", playPauseIntent))
        }
        builder.addAction(generateAction(R.drawable.ic_skip_next, "Next", nextIntent))

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = service.getString(R.string.notification_name)
            val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW)

            channel.description = service.getString(R.string.notification_name)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun generateAction(icon: Int, title: String, action: PendingIntent): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(icon, title, action).build()
    }

    private fun createBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(service, CHANNEL_ID)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_station_1)
                .setContentIntent(session.controller.sessionActivity)
                .setDeleteIntent(stopIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(mediaStyle)
    }
}
