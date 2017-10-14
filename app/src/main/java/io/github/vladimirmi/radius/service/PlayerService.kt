package io.github.vladimirmi.radius.service


import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import io.github.vladimirmi.radius.App
import io.github.vladimirmi.radius.BuildConfig
import io.github.vladimirmi.radius.R
import timber.log.Timber

/**
 * Developer Vladimir Mikhalev, 09.05.2017.
 */

class PlayerService : MediaBrowserServiceCompat(), PlaybackCallback {

    companion object {
        const val ACTION_PLAY = BuildConfig.APPLICATION_ID + ".ACTION_PLAY"
        const val ACTION_PAUSE = BuildConfig.APPLICATION_ID + ".ACTION_PAUSE"
        const val ACTION_STOP = BuildConfig.APPLICATION_ID + ".ACTION_STOP"

        const val EXTRA_STATION = "EXTRA_STATION"
    }

    private var mStationUrl: String? = null
    private lateinit var session: MediaSessionCompat
    private lateinit var playback: Playback
    private var serviceStarted: Boolean = false

    override fun onCreate() {
        super.onCreate()

        session = MediaSessionCompat(this, javaClass.simpleName)
        sessionToken = session.sessionToken
        session.setCallback(PlayerSessionCallback())
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        playback = Playback(this)
        playback.state = PlaybackStateCompat.STATE_NONE
        playback.callback = this
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
                    mStationUrl = intent.getStringExtra(EXTRA_STATION)
//                    playback.setStationUrl(mStationUrl);
                    handlePlayRequest()
                }
            }
            ACTION_PAUSE -> {
                handlePauseRequest()
            }
            ACTION_STOP -> {
                handleStopRequest()
            }
        }
        return Service.START_STICKY
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot(getString(R.string.app_name), null)
    }

    override fun onLoadChildren(parentId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(emptyList())
    }

    override fun onCompletion() {
        Timber.w("onCompletion")
        handlePlayRequest()
    }

    override fun onPlaybackStatusChanged(state: Int) = updatePlaybackState()

    override fun onError(error: String) = Timber.e(error)

    private fun handlePlayRequest() {
        val stationUrl = App.stationsRep.stations.first().uri.toString()
        Timber.d("handlePlayRequest: mState=${playback.state}, with url $stationUrl")

        if (!serviceStarted) {
            Timber.v("Starting service")
            startService(Intent(applicationContext, PlayerService::class.java))
            serviceStarted = true
        }
        session.isActive = true
        playback.play(stationUrl)
    }

    private fun handlePauseRequest() {
        Timber.d("handlePauseRequest: mState=${playback.state}")
        playback.pause()
    }

    private fun handleStopRequest() {
        Timber.d("handleStopRequest: mState=${playback.state}")
        playback.stop(true)
        stopSelf()
        serviceStarted = false
        session.isActive = false
    }

    private fun updatePlaybackState() {
        Timber.d("updatePlaybackState, playback state=" + playback.state)

        val stateBuilder = PlaybackStateCompat.Builder()
                .setActions(availableActions)
                .setState(playback.state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)

        session.setPlaybackState(stateBuilder.build())
    }

    private val availableActions: Long
        get() {
            return if (playback.isPlaying) {
                PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_PAUSE
            } else {
                PlaybackStateCompat.ACTION_PLAY
            }
        }

    inner class PlayerSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            Timber.d("play")
            handlePlayRequest()
        }

        override fun onStop() {
            Timber.d("stop. current state=" + playback.state)
            handleStopRequest()
        }

        override fun onPause() {
            Timber.d("pause. current state=" + playback.state)
            handlePauseRequest()
        }
    }
}
