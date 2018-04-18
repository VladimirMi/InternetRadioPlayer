package io.github.vladimirmi.internetradioplayer.model.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.extensions.ioToMain
import io.github.vladimirmi.internetradioplayer.extensions.toUri
import io.github.vladimirmi.internetradioplayer.model.entity.PlayerMode
import io.github.vladimirmi.internetradioplayer.model.entity.Station
import io.github.vladimirmi.internetradioplayer.model.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.model.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.presentation.root.RootActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import toothpick.Toothpick
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

/**
 * Developer Vladimir Mikhalev, 09.05.2017.
 */

class PlayerService : MediaBrowserServiceCompat(), SessionCallback.Interface {

    companion object {
        const val EVENT_SESSION_NEXT = "EVENT_SESSION_NEXT"
        const val EVENT_SESSION_PREVIOUS = "EVENT_SESSION_PREVIOUS"

        const val EXTRA_STATION_ID = "EXTRA_STATION_ID"
    }

    @Inject lateinit var stationInteractor: StationInteractor
    @Inject lateinit var controlsInteractor: PlayerControlsInteractor

    private val compDisp = CompositeDisposable()
    private lateinit var session: MediaSessionCompat
    private lateinit var playback: Playback
    private lateinit var notification: MediaNotification

    private var playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1F)
            .setActions(AvailableActions.NEXT_PREVIOUS_ENABLED).build()

    private var serviceStarted = false
    private var currentStationId: String? = null
    private var playingStationId: String? = null
    private var stopTask: TimerTask? = null

    override fun onCreate() {
        super.onCreate()
        Toothpick.inject(this, Scopes.app)

        session = MediaSessionCompat(this, javaClass.simpleName)
        session.setCallback(SessionCallback(this))
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        session.setPlaybackState(playbackState)
        val activityIntent = Intent(applicationContext, RootActivity::class.java)
        session.setSessionActivity(PendingIntent.getActivity(applicationContext, 0, activityIntent, 0))
        sessionToken = session.sessionToken

        playback = Playback(this, playerCallback)
        notification = MediaNotification(this, session, stationInteractor)

        stationInteractor.currentStationObs
                .subscribe { handleCurrentStation(it) }
                .addTo(compDisp)

        controlsInteractor.playerModeObs
                .subscribe { handlePlayerMode(it) }
                .addTo(compDisp)

        stationInteractor.currentIconObs
                .ioToMain()
                .subscribe { notification.update() }
                .addTo(compDisp)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) stopSelf()
        return Service.START_STICKY
    }

    override fun onDestroy() {
        compDisp.dispose()
        onStopCommand()
        playback.releasePlayer()
    }

    override fun onGetRoot(clientPackageName: String,
                           clientUid: Int,
                           rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {

        return MediaBrowserServiceCompat.BrowserRoot(getString(R.string.app_name), null)
    }

    override fun onLoadChildren(
            parentId: String,
            result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {

        result.sendResult(emptyList())
    }

    private fun startService() {
        if (!serviceStarted) {
            startService(Intent(applicationContext, PlayerService::class.java))
            serviceStarted = true
            session.isActive = true
        }
    }

    private fun handleCurrentStation(it: Station) {
        notification.update()
        currentStationId = it.id
        if (isPlaying && currentStationId != playingStationId) playCurrent()
    }

    private fun handlePlayerMode(playerMode: PlayerMode) {
        val actions = when (playerMode) {
            PlayerMode.NEXT_PREVIOUS_ENABLED -> AvailableActions.NEXT_PREVIOUS_ENABLED
            PlayerMode.NEXT_PREVIOUS_DISABLED -> AvailableActions.NEXT_PREVIOUS_DISABLED
            else -> return
        }
        val state = createPlaybackState(actions = actions)
        session.setPlaybackState(state)
        notification.update()
    }

    //todo use controls interactor
    private val isPlaying: Boolean
        get() {
            return session.controller.playbackState?.state.let { state ->
                state == PlaybackStateCompat.STATE_PLAYING
                        || state == PlaybackStateCompat.STATE_BUFFERING
            }
        }
    private val isPaused
        get() = session.controller.playbackState.state == PlaybackStateCompat.STATE_PAUSED


    private fun playCurrent() {
        val station = stationInteractor.currentStation
        playingStationId = station.id
        playback.play(station.uri.toUri())
    }

    //region =============== SessionCallback ==============

    override fun onPlayCommand() {
        stopTask?.cancel()
        startService()
        if (isPaused && currentStationId == playingStationId) {
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
            super.onPlayerStateChanged(playWhenReady, playbackState)
            val state = when (playbackState) {
                Player.STATE_IDLE -> PlaybackStateCompat.STATE_STOPPED
                Player.STATE_BUFFERING -> {
                    if (playWhenReady) PlaybackStateCompat.STATE_BUFFERING
                    else PlaybackStateCompat.STATE_PAUSED
                }
                Player.STATE_READY -> {
                    if (playWhenReady) PlaybackStateCompat.STATE_PLAYING
                    else PlaybackStateCompat.STATE_PAUSED
                }
                Player.STATE_ENDED -> PlaybackStateCompat.STATE_PAUSED
                else -> PlaybackStateCompat.STATE_NONE
            }

            session.setPlaybackState(createPlaybackState(state = state))
            notification.update()
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            super.onPlayerError(error)
            onStopCommand()
        }

        override fun onMetadata(metadata: Metadata) {
            super.onMetadata(metadata)
            session.setMetadata(metadata.toMediaMetadata())
            notification.update()
        }
    }

    private fun createPlaybackState(state: Int? = null, actions: Long? = null): PlaybackStateCompat {
        return PlaybackStateCompat.Builder(playbackState)
                .apply {
                    state?.let { setState(it, 0, 1F) }
                    actions?.let { setActions(it) }
                }.build()
                .also { playbackState = it }
    }
}
