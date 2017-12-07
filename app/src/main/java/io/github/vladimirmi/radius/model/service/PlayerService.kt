package io.github.vladimirmi.radius.model.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
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
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.presentation.root.RootActivity
import timber.log.Timber
import toothpick.Toothpick
import javax.inject.Inject

/**
 * Developer Vladimir Mikhalev, 09.05.2017.
 */

class PlayerService : MediaBrowserServiceCompat() {

    companion object {
        const val EXTRA_STATION_ID = "EXTRA_STATION_ID"
    }

    @Inject lateinit var repository: StationRepository

    private lateinit var session: MediaSessionCompat
    private lateinit var playback: Playback
    private lateinit var notification: MediaNotification
    private var serviceStarted: Boolean = false
    private var currentStation: Station? = null

    private val playbackState = PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_STOP or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
            .build()

    override fun onCreate() {
        super.onCreate()
        Scopes.app.apply {
            Toothpick.inject(this@PlayerService, this)
            Toothpick.closeScope(this)
        }

        session = MediaSessionCompat(this, javaClass.simpleName)
        sessionToken = session.sessionToken
        session.setCallback(SessionCallback())
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        session.setPlaybackState(playbackState)
        val activityIntent = Intent(applicationContext, RootActivity::class.java)
        session.setSessionActivity(PendingIntent.getActivity(applicationContext, 0, activityIntent, 0))

        playback = Playback(this, playerCallback)
        notification = MediaNotification(this, session)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            Timber.d("onStartCommand: Stop self")
            stopSelf()
        }
        return Service.START_STICKY
    }

    override fun onDestroy() {
        Timber.e("onDestroy: ")
        handleStopRequest()
        playback.releasePlayer()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? =
            MediaBrowserServiceCompat.BrowserRoot(getString(R.string.app_name), null)

    override fun onLoadChildren(parentId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(emptyList())
    }

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
            handleStopRequest()
        }

        override fun onMetadata(key: String, value: String) {
            session.setMetadata(createMetadata(key, value))
            notification.update()
        }

        private fun createPlaybackState(state: Int): PlaybackStateCompat {
            return PlaybackStateCompat.Builder(playbackState)
                    .setState(state, 0, 1F)
                    .build()
        }

        private fun createMetadata(key: String = "", value: String = ""): MediaMetadataCompat {
            val (artist, title) = if (value.isEmpty()) {
                listOf("", "")
            } else {
                value.split("-").map { it.trim() }
            }
            return MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentStation?.title)
                    .build()
        }
    }

    private fun startService() {
        if (!serviceStarted) {
            Timber.v("Starting service")
            startService(Intent(applicationContext, PlayerService::class.java))
            serviceStarted = true
            session.isActive = true
        }
    }

    private fun handlePlayRequest(uri: Uri) {
        Timber.d("handlePlayRequest with url $uri")
        startService()
        playback.play(uri)
    }

    private fun handleResumeRequest() {
        Timber.d("handleResumeRequest")
        playback.resume()
    }

    private fun handlePauseRequest() {
        Timber.d("handlePauseRequest")
        playback.pause()
    }

    private fun handleStopRequest() {
        Timber.d("handleStopRequest")
        playback.stop()
        stopSelf()
        serviceStarted = false
        session.isActive = false
    }

    private fun handleSkipToNextRequest() {
        Timber.d("handleSkipToNextRequest")
        currentStation = repository.next()
        currentStation?.uri?.toUri()?.let { handlePlayRequest(it) }
    }

    private fun handleSkipToPreviousRequest() {
        Timber.d("handleSkipToPreviousRequest")
        currentStation = repository.previous()
        currentStation?.uri?.toUri()?.let { handlePlayRequest(it) }
    }


    private fun handleExtras(extras: Bundle) {
        val id = extras.getString(EXTRA_STATION_ID)
        currentStation = repository.newStation ?: repository.getStation(id)
    }

    inner class SessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            handleResumeRequest()
        }

        override fun onPlayFromUri(uri: Uri, extras: Bundle?) {
            if (extras != null) handleExtras(extras)
            handlePlayRequest(uri)
        }

        override fun onPause() {
            handlePauseRequest()
        }

        override fun onStop() {
            handleStopRequest()
        }

        override fun onSkipToPrevious() {
            handleSkipToPreviousRequest()
        }

        override fun onSkipToNext() {
            handleSkipToNextRequest()
        }
    }
}
