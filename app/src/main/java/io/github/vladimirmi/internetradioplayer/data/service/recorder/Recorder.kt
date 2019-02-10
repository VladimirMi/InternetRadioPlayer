package io.github.vladimirmi.internetradioplayer.data.service.recorder

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.TeeDataSource
import com.google.android.exoplayer2.util.Util
import io.github.vladimirmi.internetradioplayer.BuildConfig
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.service.AudioRenderersFactory
import io.github.vladimirmi.internetradioplayer.data.service.ErrorHandlingPolicy
import io.github.vladimirmi.internetradioplayer.data.service.IcyHttpDataSource
import io.github.vladimirmi.internetradioplayer.extensions.runOnUiThread
import io.github.vladimirmi.internetradioplayer.extensions.wifiManager
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 09.02.2019.
 */

class Recorder
@Inject constructor(private val context: Context,
                    private val httpClient: OkHttpClient,
                    private val recorderDataSink: RecorderDataSink) {

    private var player: SimpleExoPlayer? = null

    private val wifiLock = context.wifiManager
            .createWifiLock(WifiManager.WIFI_MODE_FULL, BuildConfig.APPLICATION_ID)

    fun startRecord(uri: Uri) {
        runOnUiThread {
            if (player == null) createPlayer()
            if (!wifiLock.isHeld) wifiLock.acquire()
            preparePlayer(uri)
            player?.playWhenReady = false
        }
    }

    fun stopRecord() {
        runOnUiThread {
            player?.stop()
            releasePlayer()
            if (wifiLock.isHeld) wifiLock.release()
        }
    }

    private fun createPlayer() {
        val loadControl = DefaultLoadControl.Builder()
                .setAllocator(DefaultAllocator(true, C.DEFAULT_AUDIO_BUFFER_SIZE))
                .createDefaultLoadControl()

        player = ExoPlayerFactory.newSimpleInstance(context, AudioRenderersFactory(context),
                DefaultTrackSelector(), loadControl)

        val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build()
        player?.setAudioAttributes(audioAttributes, true)
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    private fun preparePlayer(uri: Uri) {
        val userAgent = Util.getUserAgent(context, context.getString(R.string.app_name))
        val mediaSource = ExtractorMediaSource.Factory {
            val icyHttpDataSource = IcyHttpDataSource(httpClient, userAgent, null)
            TeeDataSource(icyHttpDataSource, recorderDataSink)
        }
                .setLoadErrorHandlingPolicy(ErrorHandlingPolicy())
                .createMediaSource(uri)

        player?.prepare(mediaSource)
    }
}