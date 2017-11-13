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
import android.support.v4.media.session.PlaybackStateCompat.*
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlaybackException.*
import com.google.android.exoplayer2.Player
import io.github.vladimirmi.radius.BuildConfig
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.di.Scopes
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.ui.root.RootActivity
import timber.log.Timber
import toothpick.Toothpick
import javax.inject.Inject

/**
 * Developer Vladimir Mikhalev, 09.05.2017.
 */

class PlayerService : MediaBrowserServiceCompat() {

    companion object {
        const val ACTION_PLAY = BuildConfig.APPLICATION_ID + ".ACTION_PLAY"
        const val ACTION_PAUSE = BuildConfig.APPLICATION_ID + ".ACTION_PAUSE"
        const val ACTION_STOP = BuildConfig.APPLICATION_ID + ".ACTION_STOP"

        const val EXTRA_STATION = "EXTRA_STATION"
    }

    @Inject lateinit var stationRepository: StationRepository

    private var stationUrl: String? = null
    private lateinit var session: MediaSessionCompat
    private lateinit var playback: Playback
    private lateinit var notification: MediaNotification
    private var serviceStarted: Boolean = false

    override fun onCreate() {
        super.onCreate()
        Toothpick.openScope(Scopes.APP).apply {
            Toothpick.inject(this@PlayerService, this)
            Toothpick.closeScope(this)
        }

        session = MediaSessionCompat(this, javaClass.simpleName)
        sessionToken = session.sessionToken
        session.setCallback(SessionCallback())
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        val activityIntent = Intent(applicationContext, RootActivity::class.java)
        session.setSessionActivity(PendingIntent.getActivity(applicationContext, 0, activityIntent, 0))

        playback = Playback(this, playerCallback)
        notification = MediaNotification(this, session)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            Timber.d("onStartCommand: Stop self")
            stopSelf()
            return Service.START_STICKY
        }
        when (intent.action) {
            null -> Timber.e("onStartCommand: actions null")
            ACTION_PLAY -> {
                if (intent.hasExtra(EXTRA_STATION)) {
                    stationUrl = intent.getStringExtra(EXTRA_STATION)
                    handlePlayRequest(Uri.parse(stationUrl))
                } else {
                    handleResumeRequest()
                }
            }
            ACTION_PAUSE -> handlePauseRequest()
            ACTION_STOP -> handleStopRequest()
        }
        return Service.START_STICKY
    }

    override fun onDestroy() {
        handleStopRequest()
        playback.releasePlayer()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot(getString(R.string.app_name), null)
    }

    override fun onLoadChildren(parentId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(emptyList())
    }

    private val playerCallback = object : PlayerCallback() {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            val state = when (playbackState) {
                Player.STATE_IDLE -> STATE_STOPPED
                Player.STATE_BUFFERING -> STATE_BUFFERING
                Player.STATE_READY -> if (playWhenReady) STATE_PLAYING else STATE_PAUSED
                Player.STATE_ENDED -> STATE_PAUSED
                else -> STATE_NONE
            }

            session.setPlaybackState(createPlaybackState(state))
            session.setMetadata(createMetadata())
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
            val availableActions = if (state == STATE_PLAYING) {
                PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_PAUSE
            } else {
                PlaybackStateCompat.ACTION_PLAY
            }
            return Builder().setActions(availableActions)
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
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM,
                            stationRepository.getSelected()?.title)
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

    inner class SessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            handleResumeRequest()
        }

        override fun onPlayFromUri(uri: Uri, extras: Bundle?) {
            handlePlayRequest(uri)
        }

        override fun onPause() {
            handlePauseRequest()
        }

        override fun onStop() {
            handleStopRequest()
        }
    }
}
