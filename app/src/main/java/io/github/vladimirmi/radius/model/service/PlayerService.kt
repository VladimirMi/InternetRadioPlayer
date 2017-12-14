package io.github.vladimirmi.radius.model.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlaybackException.*
import com.google.android.exoplayer2.Player
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.extensions.toUri
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.model.source.StationIconSource
import io.github.vladimirmi.radius.presentation.root.RootActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import toothpick.Toothpick
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

/**
 * Developer Vladimir Mikhalev, 09.05.2017.
 */

class PlayerService : MediaBrowserServiceCompat(), SessionCallback.Interface {

    companion object {
        const val ACTION_CREATE_MODE = "SESSION_ACTION_CREATE_MODE"
        const val ACTION_DEFAULT_MODE = "SESSION_ACTION_DEFAULT_MODE"
    }

    @Inject lateinit var repository: StationRepository
    @Inject lateinit var iconSource: StationIconSource

    private val compDisp = CompositeDisposable()
    private lateinit var session: MediaSessionCompat
    private lateinit var playback: Playback
    private lateinit var notification: MediaNotification
    private lateinit var metadata: MediaMetadataCompat
    private var playbackState = PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_STOP or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
            .build()
    private var serviceStarted = false
    private var currentStationId: String? = null
    private var playingStationId: String? = null
    private var stopTask: TimerTask? = null

    override fun onCreate() {
        super.onCreate()
        Toothpick.inject(this, Scopes.app)

        session = MediaSessionCompat(this, javaClass.simpleName)
        session.setCallback(SessionCallback(this))
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        session.setPlaybackState(playbackState)
        val activityIntent = Intent(applicationContext, RootActivity::class.java)
        session.setSessionActivity(PendingIntent.getActivity(applicationContext, 0, activityIntent, 0))
        sessionToken = session.sessionToken

        playback = Playback(this, playerCallback)
        notification = MediaNotification(this, session)

        repository.current.subscribeBy {
            currentStationId = it.id
            if (isPlaying && currentStationId != playingStationId) playCurrent()
        }.addTo(compDisp)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) stopSelf()
        return Service.START_STICKY
    }

    override fun onDestroy() {
        compDisp.dispose()
        onStop()
        playback.releasePlayer()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? =
            MediaBrowserServiceCompat.BrowserRoot(getString(R.string.app_name), null)

    override fun onLoadChildren(parentId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(emptyList())
    }

    private fun startService() {
        if (!serviceStarted) {
            startService(Intent(applicationContext, PlayerService::class.java))
            serviceStarted = true
            session.isActive = true
        }
    }

    //region =============== SessionCallback ==============

    override fun onPlay() {
        stopTask?.cancel()
        startService()
        if (session.controller.playbackState.state == PlaybackStateCompat.STATE_PAUSED
                && currentStationId == playingStationId) {
            playback.resume()
        } else {
            playCurrent()
        }
    }

    override fun onPause() {
        playback.pause()
        stopTask = Timer().schedule(60000) {
            onStop()
        }
    }

    override fun onStop() {
        playback.stop()
        stopSelf()
        serviceStarted = false
        session.isActive = false
    }

    override fun onSkipToPrevious() {
        if (repository.previousStation() && isPlaying) playCurrent()
    }

    override fun onSkipToNext() {
        if (repository.nextStation() && isPlaying) playCurrent()
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        when (action) {
            ACTION_CREATE_MODE -> {
                playbackState = PlaybackStateCompat.Builder()
                        .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or
                                PlaybackStateCompat.ACTION_STOP)
                        .build()
            }
            ACTION_DEFAULT_MODE -> playbackState = PlaybackStateCompat.Builder()
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_STOP or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                    .build()
        }
    }

    //endregion

    private val playerCallback = object : PlayerCallback() {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            val state = when (playbackState) {
                Player.STATE_IDLE -> PlaybackStateCompat.STATE_STOPPED
                Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
                Player.STATE_READY -> if (playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
                Player.STATE_ENDED -> PlaybackStateCompat.STATE_PAUSED
                else -> PlaybackStateCompat.STATE_NONE
            }

            session.setPlaybackState(createPlaybackState(state))
            notification.update()
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            when (error?.type) {
                TYPE_RENDERER -> Timber.e("RENDERER error occurred: ${error.rendererException}")
                TYPE_SOURCE -> Timber.e("SOURCE error occurred: ${error.sourceException}")
                TYPE_UNEXPECTED -> Timber.e("UNEXPECTED error occurred: ${error.unexpectedException}")
            }
            onStop()
        }

        override fun onMetadata(key: String, value: String) {
            session.setMetadata(createMetadata(key, value))
            notification.update()
        }
    }

    private fun createMetadata(key: String = "", value: String = ""): MediaMetadataCompat {
        Timber.d("metadata $key: $value")
        val (artist, title) = if (value.contains('-')) {
            value.split("-").map { it.trim() }
        } else {
            listOf("", value)
        }
        return MediaMetadataCompat.Builder(metadata)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .build()
    }

    private fun createPlaybackState(state: Int): PlaybackStateCompat {
        return PlaybackStateCompat.Builder(playbackState)
                .setState(state, 0, 1F)
                .build()
    }

    private val isPlaying: Boolean
        get() {
            return with(session.controller.playbackState) {
                state == PlaybackStateCompat.STATE_PLAYING
                        || state == PlaybackStateCompat.STATE_BUFFERING
            }
        }

    private fun playCurrent() {
        val station = repository.current.value
        playingStationId = station.id
        playback.play(station.uri.toUri()!!)

        val iconBitmap = iconSource.getBitmap(station.title)
        metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, station.title)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, iconBitmap)
                .build()
    }
}
