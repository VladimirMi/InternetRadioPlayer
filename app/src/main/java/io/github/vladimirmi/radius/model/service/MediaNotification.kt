package io.github.vladimirmi.radius.model.service

import android.app.Notification
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.RemoteViews
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.model.repository.StationRepository


/**
 * Created by Vladimir Mikhalev 20.10.2017.
 */

class MediaNotification(private val service: PlayerService,
                        private val mediaSession: MediaSessionCompat) {
    companion object {
        const val CHANNEL_ID = "radius channel"
        const val PLAYER_NOTIFICATION_ID = 50
    }

    private val playPauseIntent = MediaButtonReceiver
            .buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_PLAY_PAUSE)
    private val stopIntent = MediaButtonReceiver
            .buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_STOP)
    private val nextIntent = MediaButtonReceiver
            .buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
    private val previousIntent = MediaButtonReceiver
            .buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)

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

    private fun getNotification(): Notification {
        val playbackState = mediaSession.controller.playbackState
        val description: MediaDescriptionCompat? = mediaSession.controller.metadata?.description

        val bitmap = Scopes.app.getInstance(StationRepository::class.java).iconBitmap

        MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_STOP)

        val notificationView = RemoteViews(service.packageName, R.layout.notification)
        with(notificationView) {

            setImageViewBitmap(R.id.icon, bitmap)

            setTextViewText(R.id.content_title, description?.title)
            setTextViewText(R.id.content_text, description?.subtitle)

            setOnClickPendingIntent(R.id.play_pause, playPauseIntent)
            setOnClickPendingIntent(R.id.previous, previousIntent)
            setOnClickPendingIntent(R.id.next, nextIntent)

            if (playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                setInt(R.id.play_pause, "setBackgroundResource", R.drawable.ic_stop)
            } else {
                setInt(R.id.play_pause, "setBackgroundResource", R.drawable.ic_play)
            }
        }

        val style = android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowCancelButton(true)
                .setCancelButtonIntent(stopIntent)

        return NotificationCompat.Builder(service, CHANNEL_ID)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_radius)
                .setContentIntent(mediaSession.controller.sessionActivity)
                .setDeleteIntent(stopIntent)
                .setStyle(style)
                .setCustomContentView(notificationView)
                .setCustomBigContentView(notificationView)
                .build()
    }
}