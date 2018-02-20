package io.github.vladimirmi.internetradioplayer.model.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.RemoteViews
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.model.interactor.StationInteractor

/**
 * Created by Vladimir Mikhalev 20.10.2017.
 */

// todo remake in MediaStyle
class MediaNotification(private val service: PlayerService,
                        private val mediaSession: MediaSessionCompat,
                        private val stationInteractor: StationInteractor) {

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

    private val notificationView = RemoteViews(service.packageName, R.layout.view_notification)
    private val notificationViewBig = RemoteViews(service.packageName, R.layout.view_notification_big)

    private val builder = NotificationCompat.Builder(service, CHANNEL_ID)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_station_1)
            .setContentIntent(mediaSession.controller.sessionActivity)
            .setDeleteIntent(stopIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val style = android.support.v4.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession.sessionToken)
                            .setShowCancelButton(true)
                            .setCancelButtonIntent(stopIntent)

                    setStyle(style)
                }
            }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    fun update() {
        when (mediaSession.controller.playbackState.state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                service.startForeground(PLAYER_NOTIFICATION_ID, getNotification())
                return
            }
            PlaybackStateCompat.STATE_STOPPED -> {
                service.stopForeground(true)
                NotificationManagerCompat.from(service).cancelAll()
                return
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                service.stopForeground(false)
            }
        }
        NotificationManagerCompat.from(service).notify(PLAYER_NOTIFICATION_ID, getNotification())
    }

    private fun getNotification(): Notification {
        return builder.setCustomContentView(notificationView.setup())
                .setCustomBigContentView(notificationViewBig.setup())
                .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channelName = service.getString(R.string.notification_name)
        val notificationChannel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW)

        // Configure the notification channel.
        notificationChannel.description = service.getString(R.string.notification_name)
        (service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(notificationChannel)
    }

    private fun RemoteViews.setup(): RemoteViews {
        val playbackState = mediaSession.controller.playbackState

        mediaSession.controller.metadata?.let {
            val metadata = Metadata.create(it)

            if (metadata.isSupported) {
                setTextViewText(R.id.titleTv, metadata.title)
                setTextViewText(R.id.artistTv, metadata.artist)
                setViewVisibility(R.id.artistTv, View.VISIBLE)
            } else {
                setTextViewText(R.id.titleTv, service.getString(R.string.metadata_not_available))
                setViewVisibility(R.id.artistTv, View.GONE)
            }
        }

        if (playbackState.state == PlaybackStateCompat.STATE_BUFFERING) {
            setTextViewText(R.id.titleTv, service.getString(R.string.metadata_buffering))
            setViewVisibility(R.id.artistTv, View.GONE)
        }

        setTextViewText(R.id.stationTv, stationInteractor.currentStation.name)
        setImageViewBitmap(R.id.iconIv, stationInteractor.currentIcon.bitmap)

        if (AvailableActions.isNextPreviousEnabled(playbackState.actions)) {
            setViewVisibility(R.id.previousBt, View.VISIBLE)
            setViewVisibility(R.id.nextBt, View.VISIBLE)
        } else {
            setViewVisibility(R.id.previousBt, View.GONE)
            setViewVisibility(R.id.nextBt, View.GONE)
        }

        if (playbackState.state == PlaybackStateCompat.STATE_STOPPED
                || playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
            setInt(R.id.playPauseBt, "setBackgroundResource", R.drawable.ic_play)
        } else {
            setInt(R.id.playPauseBt, "setBackgroundResource", R.drawable.ic_stop)
        }

        setOnClickPendingIntent(R.id.playPauseBt, playPauseIntent)
        setOnClickPendingIntent(R.id.previousBt, previousIntent)
        setOnClickPendingIntent(R.id.nextBt, nextIntent)

        return this
    }
}
