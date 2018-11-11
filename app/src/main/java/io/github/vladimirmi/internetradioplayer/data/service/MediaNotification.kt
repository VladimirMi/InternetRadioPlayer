package io.github.vladimirmi.internetradioplayer.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import io.github.vladimirmi.internetradioplayer.R

/**
 * Created by Vladimir Mikhalev 20.10.2017.
 */

private const val CHANNEL_ID = "internet_radio_player_channel"
private const val PLAYER_NOTIFICATION_ID = 73

class MediaNotification(private val service: PlayerService,
                        private val session: MediaSessionCompat) {

    private val notificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val playPauseIntent = PlayerActions.playPauseIntent(service.applicationContext)
    private val stopIntent = PlayerActions.stopIntent(service.applicationContext)
    private val nextIntent = PlayerActions.nextIntent(service.applicationContext)
    private val previousIntent = PlayerActions.previousIntent(service.applicationContext)

    private val mediaStyle = MediaStyle()
            .setMediaSession(session.sessionToken)
            .setShowCancelButton(true)
            .setShowActionsInCompactView(0, 1, 2)
            .setCancelButtonIntent(stopIntent)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    fun update() {
        val state = session.controller.playbackState.state
        when (state) {
            PlaybackStateCompat.STATE_PLAYING -> service.startForeground(PLAYER_NOTIFICATION_ID,
                    createNotification())
            PlaybackStateCompat.STATE_STOPPED -> service.stopForeground(true)
            else -> {
                if (state == PlaybackStateCompat.STATE_PAUSED) {
                    service.stopForeground(false)
                }
                notificationManager.notify(PLAYER_NOTIFICATION_ID, createNotification())
            }
        }
    }

    private fun createNotification(): Notification {
        val builder = createBuilder()
        val metadata: MediaMetadataCompat? = session.controller.metadata
        val playbackState = session.controller.playbackState.state

        metadata?.let {
            builder.setLargeIcon(it.art)
                    .setContentTitle(it.description.title)
                    .setContentText(it.description.subtitle)
                    .setSubText(it.description.description)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channelName = service.getString(R.string.notification_name)
        val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW)

        channel.description = service.getString(R.string.notification_name)
        notificationManager.createNotificationChannel(channel)
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
