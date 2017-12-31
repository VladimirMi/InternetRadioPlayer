package io.github.vladimirmi.radius.model.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
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
import io.github.vladimirmi.radius.extensions.ioToMain
import io.github.vladimirmi.radius.extensions.toUri
import io.github.vladimirmi.radius.model.entity.PlayerMode
import io.github.vladimirmi.radius.model.interactor.PlayerControlsInteractor
import io.github.vladimirmi.radius.model.interactor.StationInteractor
import io.github.vladimirmi.radius.presentation.root.RootActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import toothpick.Toothpick
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.schedule

/**
 * Developer Vladimir Mikhalev, 09.05.2017.
 */

class PlayerService : MediaBrowserServiceCompat(), SessionCallback.Interface {

    companion object {
        const val EVENT_SESSION_NEXT = "EVENT_SESSION_NEXT"
        const val EVENT_SESSION_PREVIOUS = "EVENT_SESSION_PREVIOUS"
    }

    @Inject lateinit var stationInteractor: StationInteractor
    @Inject lateinit var controlsInteractor: PlayerControlsInteractor

    private val compDisp = CompositeDisposable()
    private lateinit var session: MediaSessionCompat
    private lateinit var playback: Playback
    private lateinit var notification: MediaNotification
    private var metadata = MediaMetadataCompat.Builder().build()
    private var playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1F).build()
    private var serviceStarted = false
    private var currentStationId: String? = null
    private var playingStationId: String? = null
    private var stopTask: TimerTask? = null

    override fun onCreate() {
        Timber.e("onCreate: ")
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

        stationInteractor.currentStationObs().subscribe {
            //            currentStationId = it.id
            if (isPlaying
//                    && currentStationId != playingStationId
                    ) playCurrent()
        }
                .addTo(compDisp)

        controlsInteractor.playerModeObs.delaySubscription(1000, TimeUnit.MILLISECONDS)
                .subscribe {
                    val actions = when (it!!) {
                        PlayerMode.NEXT_PREVIOUS_ENABLED -> AvailableActions.NEXT_PREVIOUS_ENABLED
                        PlayerMode.NEXT_PREVIOUS_DISABLED -> AvailableActions.NEXT_PREVIOUS_DISABLED
                    }
                    session.setPlaybackState(createPlaybackState(actions = actions))
                    notification.update()
                }.addTo(compDisp)

        stationInteractor.currentIconObs()
                .ioToMain()
                .subscribe { updateIcon(it.bitmap) }
                .addTo(compDisp)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.e("onStartCommand: ")
        if (intent == null) stopSelf()
        return Service.START_STICKY
    }

    override fun onDestroy() {
        compDisp.dispose()
        onStopCommand()
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

    override fun onPlayCommand() {
        stopTask?.cancel()
        startService()
        if (session.controller.playbackState.state == PlaybackStateCompat.STATE_PAUSED
//                && currentStationId == playingStationId
                ) {
            playback.resume()
        } else {
            playCurrent()
        }
    }

    override fun onPauseCommand(stopDelay: Long) { // default is 1 min
        playback.pause()
        stopTask = Timer().schedule(stopDelay) {
            onStopCommand()
        }
    }

    override fun onStopCommand() {
        playback.stop()
        stopSelf()
        serviceStarted = false
        session.isActive = false
    }

    override fun onSkipToPreviousCommand() {
        if (controlsInteractor.previousStation()) {
            session.sendSessionEvent(EVENT_SESSION_PREVIOUS, null)
        }
    }

    override fun onSkipToNextCommand() {
        if (controlsInteractor.nextStation()) {
            session.sendSessionEvent(EVENT_SESSION_NEXT, null)
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

            session.setPlaybackState(createPlaybackState(state = state))
            notification.update()
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            when (error?.type) {
                TYPE_RENDERER -> Timber.e("RENDERER error occurred: ${error.rendererException}")
                TYPE_SOURCE -> Timber.e("SOURCE error occurred: ${error.sourceException}")
                TYPE_UNEXPECTED -> Timber.e("UNEXPECTED error occurred: ${error.unexpectedException}")
            }
            onStopCommand()
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

    private fun createPlaybackState(state: Int? = null, actions: Long? = null): PlaybackStateCompat {
        playbackState = PlaybackStateCompat.Builder(playbackState).apply {
            state?.let { setState(it, 0, 1F) }
            actions?.let { setActions(it) }
        }.build()
        return playbackState
    }

    private val isPlaying: Boolean
        get() {
            return session.controller.playbackState?.state.let { state ->
                state == PlaybackStateCompat.STATE_PLAYING
                        || state == PlaybackStateCompat.STATE_BUFFERING
            }
        }

    private fun playCurrent() {
        val station = stationInteractor.currentStation
//        playingStationId = station.id

        metadata = MediaMetadataCompat.Builder(metadata)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, station.name)
                .build()
        station.uri.toUri()?.let { playback.play(it) }
    }

    private fun updateIcon(bitmap: Bitmap) {
        metadata = MediaMetadataCompat.Builder(metadata)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)
                .build()
        notification.update()
    }
}
