package io.github.vladimirmi.radius.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.*
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultAllocator
import io.github.vladimirmi.radius.BuildConfig
import io.github.vladimirmi.radius.service.Playback.AudioFocus.*
import timber.log.Timber


class Playback(private val service: PlayerService)
    : AudioManager.OnAudioFocusChangeListener {

    companion object {
        private val VOLUME_DUCK = 0.2f
        private val VOLUME_NORMAL = 1.0f
    }

    enum class AudioFocus {
        NO_FOCUSED_NO_DUCK,
        NO_FOCUSED_CAN_DUCK,
        FOCUSED
    }

    private var audioFocus = NO_FOCUSED_NO_DUCK
    private var playAgainOnFocus = false

    private val wifiLock = (service.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createWifiLock(WifiManager.WIFI_MODE_FULL, BuildConfig.APPLICATION_ID)
    private val audioManager = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var mediaPlayer: SimpleExoPlayer? = null
    lateinit var callback: PlaybackCallback
    var state: Int = STATE_NONE

    val isPlaying: Boolean
        get() = playAgainOnFocus || mediaPlayer != null && mediaPlayer!!.playWhenReady

    fun play(stationUrl: String) {
        playAgainOnFocus = true
        tryToGetAudioFocus()
        registerAudioNoisyReceiver()

        if (state == STATE_PAUSED && mediaPlayer != null) {
            configMediaPlayerState()
        } else {
            state = STATE_STOPPED
            releaseResources(releaseMediaPlayer = false)
            try {
                createMediaPlayerIfNeeded()
                state = STATE_BUFFERING

                mediaPlayer?.apply {
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                    setDataSource(stationUrl)
                    prepareAsync()
                }
                wifiLock.acquire()
                callback.onPlaybackStatusChanged(state)


            } catch (ex: Exception) {
                Timber.e(ex, "Exception playing song")
                callback.onError(ex.message ?: "Exception playing song")
            }
        }
    }

    fun pause() {
        if (state == STATE_PLAYING) {
            // Pause media player and cancel the 'foreground service' state.
            mediaPlayer?.run { if (isPlaying) pause() }
            giveUpAudioFocus()
            releaseResources(releaseMediaPlayer = false)
        }
        state = STATE_PAUSED
        callback.onPlaybackStatusChanged(state)
        unregisterAudioNoisyReceiver()
    }

    fun stop(notifyListeners: Boolean) {
        state = STATE_STOPPED
        if (notifyListeners) {
            callback.onPlaybackStatusChanged(state)
        }
        giveUpAudioFocus()
        unregisterAudioNoisyReceiver()
        releaseResources(releaseMediaPlayer = true)
    }

    /**
     * Try to get the system audio focus.
     */
    private fun tryToGetAudioFocus() {
        if (audioFocus != FOCUSED) {
            Timber.d("tryToGetAudioFocus")
            val result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AUDIOFOCUS_GAIN)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocus = FOCUSED
            }
        }
    }

    /**
     * Give up the audio focus.
     */
    private fun giveUpAudioFocus() {
        val mPlaybackAttributes = AudioAttributes.DEFAULT
        if (audioFocus == FOCUSED) {
            Timber.d("giveUpAudioFocus")
            if (audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocus = NO_FOCUSED_NO_DUCK
            }
        }
    }

    /**
     * Called by AudioManager on audio focus changes.
     * Implementation of [AudioManager.OnAudioFocusChangeListener]
     */
    override fun onAudioFocusChange(focusChange: Int) {
        Timber.d("onAudioFocusChange: focusChange=$focusChange")

        audioFocus = when (focusChange) {
            AUDIOFOCUS_GAIN -> FOCUSED
            AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> NO_FOCUSED_CAN_DUCK
            AUDIOFOCUS_LOSS, AUDIOFOCUS_LOSS_TRANSIENT -> {
                playAgainOnFocus = state == STATE_PLAYING
                NO_FOCUSED_NO_DUCK
            }
            else -> NO_FOCUSED_NO_DUCK
        }

        configMediaPlayerState()
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and
     * starts/restarts it.
     */
    private fun configMediaPlayerState() {
        Timber.d("configMediaPlayerState. audioFocus=", audioFocus)
        if (audioFocus == NO_FOCUSED_NO_DUCK) {
            // If we don't have audio focus and can't duck, we have to pause,
            if (state == STATE_PLAYING) {
                pause()
            }
        } else {  // we have audio focus:
            // if can duck we'll be quiet else loud again
            val volume = if (audioFocus == NO_FOCUSED_CAN_DUCK) VOLUME_DUCK else VOLUME_NORMAL
            mediaPlayer?.setVolume(volume, volume)

            // If we were playing when we lost focus, we need to resume playing.
            if (playAgainOnFocus) {
                mediaPlayer?.run { if (!isPlaying) start() }
                state = STATE_PLAYING
                playAgainOnFocus = false
            }
        }
        callback.onPlaybackStatusChanged(state)
    }

    private fun createMediaPlayerIfNeeded() {
        Timber.d("createMediaPlayerIfNeeded. needed? ${mediaPlayer == null}")
        if (mediaPlayer == null) {
            val renderersFactory = DefaultRenderersFactory(service.applicationContext)
            val trackSelector = DefaultTrackSelector()
            val loadControl = DefaultLoadControl(DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE * 2))
            mediaPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl)
        }
    }

    private fun releaseResources(releaseMediaPlayer: Boolean) {
        Timber.d("releaseResources. releaseMediaPlayer=$releaseMediaPlayer")
        if (releaseMediaPlayer) {
            mediaPlayer?.release()
            mediaPlayer = null
        }
        service.stopForeground(true)

        if (wifiLock.isHeld) wifiLock.release()
    }

    private val audioNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                Timber.d("Headphones disconnected.")
                if (isPlaying) {
                    val i = Intent(context, PlayerService::class.java).apply {
                        action = PlayerService.ACTION_PAUSE
                    }
                    service.startService(i)
                }
            }
        }
    }
    @Volatile private var audioNoisyReceiverRegistered = false

    private fun registerAudioNoisyReceiver() {
        if (!audioNoisyReceiverRegistered) {
            service.registerReceiver(audioNoisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
            audioNoisyReceiverRegistered = true
        }
    }

    private fun unregisterAudioNoisyReceiver() {
        if (audioNoisyReceiverRegistered) {
            service.unregisterReceiver(audioNoisyReceiver)
            audioNoisyReceiverRegistered = false
        }
    }
}
