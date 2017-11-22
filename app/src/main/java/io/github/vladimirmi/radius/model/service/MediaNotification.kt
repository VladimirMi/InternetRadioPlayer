package io.github.vladimirmi.radius.model.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.RemoteViews
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.ui.root.RootActivity


/**
 * Created by Vladimir Mikhalev 20.10.2017.
 */

class MediaNotification(private val service: PlayerService,
                        private val mediaSession: MediaSessionCompat) {
    companion object {
        const val PENDING_PLAY_REQ = 100
        const val PENDING_PAUSE_REQ = 101
        const val PENDING_STOP_REQ = 102
        const val PENDING_NEXT_REQ = 103
        const val PENDING_PREVIOUS_REQ = 104
        const val PENDING_OPEN_REQ = 110


        const val CHANNEL_ID = "radius channel"
        const val PLAYER_NOTIFICATION_ID = 50
    }

    private val playIntent = controlsIntent(PENDING_PLAY_REQ)
    private val pauseIntent = controlsIntent(PENDING_PAUSE_REQ)
    private val stopIntent = controlsIntent(PENDING_STOP_REQ)
    private val nextIntent = controlsIntent(PENDING_NEXT_REQ)
    private val previousIntent = controlsIntent(PENDING_PREVIOUS_REQ)

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
        val metadata: MediaMetadataCompat? = mediaSession.controller.metadata

        val openIntent = Intent(service, RootActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(service, PENDING_OPEN_REQ, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)


        val notificationView = RemoteViews(service.packageName, R.layout.notification)
        with(notificationView) {
            setOnClickPendingIntent(R.id.notification, openPendingIntent)

            val bitmap = Scopes.app.getInstance(StationRepository::class.java).iconBitmap
            setImageViewBitmap(R.id.icon, bitmap)

            setTextViewText(R.id.content_title,
                    metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE) ?: "")
            setTextViewText(R.id.content_text,
                    metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST) ?: "")

            if (playbackState.actions == PlaybackStateCompat.ACTION_PLAY) {
                setOnClickPendingIntent(R.id.play_pause, playIntent)
                setInt(R.id.play_pause, "setBackgroundResource", R.drawable.ic_play)
            } else {
                setOnClickPendingIntent(R.id.play_pause, pauseIntent)
                setInt(R.id.play_pause, "setBackgroundResource", R.drawable.ic_stop)
            }

            setOnClickPendingIntent(R.id.previous, previousIntent)
            setOnClickPendingIntent(R.id.next, nextIntent)
        }

        val style = android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowCancelButton(true)
                .setCancelButtonIntent(stopIntent)

        val builder = NotificationCompat.Builder(service, CHANNEL_ID)
                .setShowWhen(false)
                .setDeleteIntent(stopIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_radius)
                .setContentIntent(openPendingIntent)
//                .setStyle(style)
                .setCustomContentView(notificationView)
                .setColor(ContextCompat.getColor(service, R.color.grey_300))

        return builder.build()
    }

    private fun controlsIntent(requestCode: Int): PendingIntent {
        val action = when (requestCode) {
            PENDING_PLAY_REQ -> PlayerService.ACTION_PLAY
            PENDING_PAUSE_REQ -> PlayerService.ACTION_PAUSE
            PENDING_STOP_REQ -> PlayerService.ACTION_STOP
            PENDING_NEXT_REQ -> PlayerService.ACTION_SKIP_TO_NEXT
            PENDING_PREVIOUS_REQ -> PlayerService.ACTION_SKIP_TO_PREVIOUS
            else -> throw IllegalArgumentException()
        }
        return PendingIntent.getService(service, requestCode,
                Intent(service, PlayerService::class.java).apply { this.action = action },
                PendingIntent.FLAG_UPDATE_CURRENT)
    }
}