package io.github.vladimirmi.internetradioplayer.data.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.interactor.EqualizerInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.FavoriteListInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.HistoryInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.errorHandler
import io.github.vladimirmi.internetradioplayer.extensions.toUri
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
        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        const val EXTRA_STATION_ID = "EXTRA_STATION_ID"
    }

    @Inject lateinit var stationInteractor: StationInteractor
    @Inject lateinit var equalizerInteractor: EqualizerInteractor
    @Inject lateinit var favoriteListInteractor: FavoriteListInteractor
    @Inject lateinit var historyInteractor: HistoryInteractor

    private val subs = CompositeDisposable()
    private lateinit var session: MediaSessionCompat
    private lateinit var playback: Playback
    private lateinit var notification: MediaNotification

    private var playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1F)
            .setActions(PlayerActions.DEFAULT_ACTIONS).build()
    private var mediaMetadata = EMPTY_METADATA

    private var serviceStarted = false
    private var currentStationId: String? = null
    private var playingStationId: String? = null
    private var stopTask: TimerTask? = null

    override fun onCreate() {
        super.onCreate()
        Toothpick.inject(this, Scopes.app)
        initSession()

        playback = Playback(this, playerCallback)
        notification = MediaNotification(this, session)

        equalizerInteractor.initPresets()
                .subscribe()
                .addTo(subs)

        equalizerInteractor.initEqualizer()
                .subscribe()
                .addTo(subs)

        stationInteractor.stationObs
                .distinctUntilChanged(Station::id)
                .subscribe { handleCurrentStation(it) }
                .addTo(subs)
    }

    private fun initSession() {
        session = MediaSessionCompat(this, javaClass.simpleName)
        session.setCallback(SessionCallback(this))
        session.setPlaybackState(playbackState)
        session.setMetadata(mediaMetadata)
        session.setSessionActivity(PlayerActions.sessionActivity(this))
        sessionToken = session.sessionToken
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) stopSelf()
        return Service.START_STICKY
    }

    override fun onDestroy() {
        subs.dispose()
        serviceStarted = false
        session.isActive = false
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

    private fun handleCurrentStation(station: Station) {
        currentStationId = station.id
        if (isPlaying) historyInteractor.createHistory(station)
        if (isPlaying && currentStationId != playingStationId) playCurrent()
        mediaMetadata = EMPTY_METADATA.setStation(station, this)
        session.setMetadata(mediaMetadata)
        notification.update()
    }

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
        val station = stationInteractor.station
        playingStationId = station.id
        playback.play(station.uri.toUri())
    }

    //region =============== SessionCallback ==============

    override fun onPlayCommand() {
        stopTask?.cancel()
        startService()
        if (isPaused && currentStationId == playingStationId) playback.resume()
        else {
            historyInteractor.createHistory(stationInteractor.station)
            playCurrent()
        }
    }

    override fun onPauseCommand(stopDelay: Long) { // default is 1 min
        playback.pause()
        scheduleStopTask(stopDelay)
    }

    override fun onStopCommand() {
        playback.stop()
        stopSelf()
    }

    override fun onSkipToPreviousCommand() {
        val changed = favoriteListInteractor.previousStation(stationInteractor.station.id)
        if (changed) session.sendSessionEvent(EVENT_SESSION_PREVIOUS, Bundle.EMPTY)
    }

    override fun onSkipToNextCommand() {
        val changed = favoriteListInteractor.nextStation(stationInteractor.station.id)
        if (changed) session.sendSessionEvent(EVENT_SESSION_NEXT, Bundle.EMPTY)
    }

    //endregion

    private val playerCallback = object : PlayerCallback() {

        override fun onPlayerStateChanged(state: Int) {
            playbackState = PlaybackStateCompat.Builder(playbackState)
                    .setState(state, 0, 1f)
                    .build()
            session.setPlaybackState(playbackState)
            if (state == PlaybackStateCompat.STATE_STOPPED) {
                mediaMetadata = mediaMetadata.clear()
                session.setMetadata(mediaMetadata)
            }
            notification.update()
        }

        override fun onPlayerError(error: Exception) {
            errorHandler.invoke(error)
        }

        override fun onMetadata(metadata: String) {
            mediaMetadata = mediaMetadata.setArtistTitle(metadata)
            session.setMetadata(mediaMetadata)
            notification.update()
        }

        override fun onAudioSessionId(event: String, audioSessionId: Int) {
            session.sendSessionEvent(event, Bundle().apply { putInt(EXTRA_SESSION_ID, audioSessionId) })
        }
    }

    private fun scheduleStopTask(stopDelay: Long) {
        stopTask?.cancel()
        stopTask = Timer().schedule(stopDelay) {
            onStopCommand()
        }
    }
}
