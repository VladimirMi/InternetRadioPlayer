package io.github.vladimirmi.radius.presentation.playercontrol

import android.arch.lifecycle.Observer
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.model.repository.MediaBrowserController
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.ui.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

@InjectViewState
class PlayerControlPresenter
@Inject constructor(private val browserController: MediaBrowserController,
                    private val repository: StationRepository)
    : BasePresenter<PlayerControlView>() {

    override fun onFirstAttach() {
        browserController.playbackState.observe(this, Observer { handleState(it) })
        browserController.playbackMetaData.observe(this, Observer { handleMetadata(it) })
        repository.selectedData.observe(this, Observer {
            repository.getSelected()?.let {
                viewState.setMedia(it)
                if (browserController.playbackState.value?.state == PlaybackStateCompat.STATE_PLAYING) {
                    browserController.play(it.uri)
                }
            }
        })
    }

    private fun handleMetadata(metadata: MediaMetadataCompat?) {
        val artist = metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
        val title = metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
        val info = if (artist.isNullOrEmpty()) "" else artist +
                if (title.isNullOrEmpty()) "" else " - $title"

        if (!info.isEmpty()) viewState.setMediaInfo(info)
    }

    private fun handleState(state: PlaybackStateCompat?) {
        when (state?.state) {
            STATE_BUFFERING -> viewState.showBuffering()
            STATE_PAUSED, STATE_STOPPED -> viewState.showStopped()
            STATE_PLAYING -> viewState.showPlaying()
        }
    }

    fun playPause() {
        val uri = repository.getSelected()?.uri ?: return
        if (browserController.isPlaying(uri)) {
            browserController.stop()
        } else {
            browserController.play(uri)
        }
    }

    fun switchFavorite() {
        val selected = repository.getSelected() ?: return
        val copy = selected.copy(fav = !selected.fav)
        repository.updateAndSave(copy)
        viewState.setMedia(copy)
    }
}