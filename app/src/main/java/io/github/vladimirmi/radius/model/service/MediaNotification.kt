package io.github.vladimirmi.radius.model.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.RemoteViews
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.model.service.PlayerService.Companion.ACTION_PAUSE
import io.github.vladimirmi.radius.model.service.PlayerService.Companion.ACTION_PLAY
import io.github.vladimirmi.radius.model.service.PlayerService.Companion.ACTION_STOP
import io.github.vladimirmi.radius.ui.root.RootActivity


/**
 * Created by Vladimir Mikhalev 20.10.2017.
 */

class MediaNotification(private val service: PlayerService,
                        private val mediaSession: MediaSessionCompat) {
    companion object {
        const val PENDING_PLAY_REQ = 100
        const val PENDING_STOP_REQ = 101
        const val PENDING_PAUSE_REQ = 102
        const val PENDING_OPEN_REQ = 103


        const val CHANNEL_ID = "radius channel"
        const val PLAYER_NOTIFICATION_ID = 50
    }

    fun update() {
        when (mediaSession.controller.playbackState.state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                service.startForeground(PLAYER_NOTIFICATION_ID, getNotification())
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                service.stopForeground(false)
                NotificationManagerCompat.from(service)
                        .notify(PLAYER_NOTIFICATION_ID, getNotification())
            }
            PlaybackStateCompat.STATE_STOPPED -> {
                service.stopForeground(true)
                NotificationManagerCompat.from(service).cancelAll()
            }
        }
    }

    //todo enum with pending intents
    private fun getNotification(): Notification {
        val playbackState = mediaSession.controller.playbackState
        val metadata: MediaMetadataCompat? = mediaSession.controller.metadata

        val playIntent = Intent(service, PlayerService::class.java).apply {
            action = ACTION_PLAY
        }
        val playPendingIntent = PendingIntent.getService(service, PENDING_PLAY_REQ, playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val stopIntent = Intent(service, PlayerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(service, PENDING_STOP_REQ, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val pauseIntent = Intent(service, PlayerService::class.java).apply {
            action = ACTION_PAUSE
        }
        val pausePendingIntent = PendingIntent.getService(service, PENDING_PAUSE_REQ, pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val openIntent = Intent(service, RootActivity::class.java)
        val openPendingIntent = PendingIntent.getService(service, PENDING_OPEN_REQ, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val style = android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowCancelButton(true)
                .setCancelButtonIntent(stopPendingIntent)

        val notificationView = RemoteViews(service.packageName, R.layout.notification)
        with(notificationView) {
            setImageViewResource(R.id.big_icon, R.drawable.ic_radius)

            setTextViewText(R.id.content_title,
                    metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE) ?: "")
            setTextViewText(R.id.content_text,
                    metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST) ?: "")

            if (playbackState.actions == PlaybackStateCompat.ACTION_PLAY) {
                setOnClickPendingIntent(R.id.play_pause, playPendingIntent)
                setInt(R.id.play_pause, "setBackgroundResource", R.drawable.ic_play)
            } else {
                setOnClickPendingIntent(R.id.play_pause, pausePendingIntent)
                setInt(R.id.play_pause, "setBackgroundResource", R.drawable.ic_stop)
            }
        }

        val builder = NotificationCompat.Builder(service, CHANNEL_ID)
                .setShowWhen(false)
                .setDeleteIntent(stopPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_radius)
                .setContentIntent(openPendingIntent)
                .setStyle(style)
                .setCustomContentView(notificationView)

        return builder.build()
    }
}