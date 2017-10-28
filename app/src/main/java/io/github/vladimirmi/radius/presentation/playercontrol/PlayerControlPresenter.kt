package io.github.vladimirmi.radius.presentation.playercontrol

import android.arch.lifecycle.Observer
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.model.entity.Media
import io.github.vladimirmi.radius.model.repository.MediaBrowserController
import io.github.vladimirmi.radius.model.repository.MediaRepository
import io.github.vladimirmi.radius.ui.base.BasePresenter
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

@InjectViewState
class PlayerControlPresenter
@Inject constructor(private val browserController: MediaBrowserController,
                    private val mediaRepository: MediaRepository)
    : BasePresenter<PlayerControlView>() {

    private val callback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            when (state.state) {
                STATE_BUFFERING -> viewState.showBuffering()
                STATE_PAUSED, STATE_STOPPED -> viewState.showStopped()
                STATE_PLAYING -> viewState.showPlaying()
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            val artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
            val title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            val info = if (artist.isNullOrEmpty()) "" else artist +
                    if (title.isNullOrEmpty()) "" else " - $title"

            if (!info.isEmpty()) viewState.setMediaInfo(info)
        }
    }

    override fun onFirstAttach() {
        browserController.registerCallback(callback)
        mediaRepository.selectedMediaData.observe(this, Observer<Media> {
            it?.let { browserController.play(it.uri) }
        })
    }

    override fun onDestroy() {
        browserController.unRegisterCallback(callback)
        super.onDestroy()
    }

    fun playPause() {
        val uri = mediaRepository.selectedMediaData.value?.uri ?: return
        Timber.e("playPause: $uri")
        if (browserController.isPlaying(uri)) {
            browserController.stop()
        } else {
            browserController.play(uri)
        }
    }
}