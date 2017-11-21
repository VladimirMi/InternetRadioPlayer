package io.github.vladimirmi.radius.presentation.playerControl

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.Screens
import io.github.vladimirmi.radius.model.repository.MediaBrowserController
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

@InjectViewState
class PlayerControlPresenter
@Inject constructor(private val browserController: MediaBrowserController,
                    private val repository: StationRepository,
                    private val router: Router)
    : BasePresenter<PlayerControlView>() {

    override fun onFirstViewAttach() {
        browserController.playbackState
                .subscribeBy { this.handleState(it) }
                .addTo(compDisp)

        browserController.playbackMetaData
                .subscribeBy { this.handleMetadata(it) }
                .addTo(compDisp)

        repository.selected
                .subscribeBy {
                    viewState.setMedia(it)
                    if (browserController.playbackState.value?.state == PlaybackStateCompat.STATE_PLAYING) {
                        browserController.play(it.uri)
                    }
                }
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
        val uri = repository.selected.value?.uri ?: return
        if (browserController.isPlaying(uri)) {
            browserController.stop()
        } else {
            browserController.play(uri)
        }
    }

    fun switchFavorite() {
        val selected = repository.selected.value ?: return
        val copy = selected.copy(fav = !selected.fav)
        repository.update(copy)
        viewState.setMedia(copy)
    }

    fun showStation() {
        router.navigateTo(Screens.STATION_SCREEN, repository.selected.value)
    }
}