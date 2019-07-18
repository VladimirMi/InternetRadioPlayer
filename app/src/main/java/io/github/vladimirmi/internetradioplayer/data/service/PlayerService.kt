package io.github.vladimirmi.internetradioplayer.data.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.preference.Preferences
import io.github.vladimirmi.internetradioplayer.data.service.extensions.PlayerActions
import io.github.vladimirmi.internetradioplayer.data.service.player.Playback
import io.github.vladimirmi.internetradioplayer.data.utils.AudioEffects
import io.github.vladimirmi.internetradioplayer.di.Scopes
import io.github.vladimirmi.internetradioplayer.domain.interactor.EqualizerInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.HistoryInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.MediaInteractor
import io.github.vladimirmi.internetradioplayer.domain.model.Media
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

    @Inject lateinit var mediaInteractor: MediaInteractor
    @Inject lateinit var equalizerInteractor: EqualizerInteractor
    @Inject lateinit var historyInteractor: HistoryInteractor
    @Inject lateinit var preferences: Preferences

    private val subs = CompositeDisposable()
    private lateinit var session: MediaSessionCompat
    private lateinit var playback: Playback
    private lateinit var notification: MediaNotification

    private var serviceStarted = false
    private var currentStationId: String? = null
    private var playingMediaId: String? = null
    private var stopTask: TimerTask? = null

    override fun onCreate() {
        super.onCreate()
        Toothpick.inject(this, Scopes.app)

        playback = Playback(this, playerCallback, preferences)
        initSession()
        initEqualizer()

        mediaInteractor.currentMediaObs
                .distinctUntilChanged(Media::uri)
                .subscribe { handleCurrentMedia(it) }
                .addTo(subs)
    }

    private fun initSession() {
        session = MediaSessionCompat(this, javaClass.simpleName)
        session.setCallback(SessionCallback(this))
        session.setSessionActivity(PlayerActions.sessionActivity(this))
        sessionToken = session.sessionToken
        notification = MediaNotification(this, session)
        playerCallback.initDefault()
        session.isActive = true
    }

    private fun initEqualizer() {
        if (!AudioEffects.isEqualizerSupported()) return
        equalizerInteractor.initPresets()
                .subscribe()
                .addTo(subs)
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
                           rootHints: Bundle?): BrowserRoot? {

        return BrowserRoot(getString(R.string.app_name), null)
    }

    override fun onLoadChildren(
            parentId: String,
            result: Result<List<MediaBrowserCompat.MediaItem>>) {

        result.sendResult(emptyList())
    }

    private fun handleCurrentMedia(media: Media) {
        playerCallback.setMedia(media)
        currentStationId = media.id
        if (isPlaying && currentStationId != playingMediaId) prepareAndPlay()
        else playback.stop()
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


    private fun prepareAndPlay() {
        stopTask?.cancel()
        val media = mediaInteractor.currentMedia
        //todo refactor (move)
        if (media is Station) historyInteractor.createHistory(media)
        playingMediaId = media.id
        playback.prepare(media.uri.toUri())
        playback.play()
    }

    //region =============== SessionCallback ==============

    override fun onPlayCommand() {
        stopTask?.cancel()
        if (isPaused && currentStationId == playingMediaId) playback.play()
        else prepareAndPlay()
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
        mediaInteractor.previousMedia()
        session.sendSessionEvent(EVENT_SESSION_PREVIOUS, Bundle.EMPTY)
    }

    override fun onSkipToNextCommand() {
        mediaInteractor.nextMedia()
        session.sendSessionEvent(EVENT_SESSION_NEXT, Bundle.EMPTY)
    }

    override fun onSeekCommand(pos: Long) {
        playback.seekTo(pos)
    }

    override fun onActionsChangeCommand(changer: (Long) -> Long) {
        playerCallback.changeActions(changer)
    }

    //endregion

    private val playerCallback = object : PlayerCallback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            try {
                session.setPlaybackState(state)
                notification.update()
            } catch (ignore: Exception) {
            }
        }

        override fun onPlayerError(error: Exception) {
            errorHandler.invoke(error)
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadataCompat) {
            session.setMetadata(mediaMetadata)
            notification.update()
        }

        override fun onAudioSessionId(event: String, audioSessionId: Int) {
            session.sendSessionEvent(event, Bundle().apply { putInt(EXTRA_SESSION_ID, audioSessionId) })
        }
    }

    private fun scheduleStopTask(stopDelay: Long) {
        //todo move to SessionCallback
        stopTask?.cancel()
        stopTask = Timer().schedule(stopDelay) {
            onStopCommand()
        }
    }
}
