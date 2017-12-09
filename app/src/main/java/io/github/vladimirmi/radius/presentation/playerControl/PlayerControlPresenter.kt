package io.github.vladimirmi.radius.presentation.playerControl

import android.graphics.Bitmap
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.model.repository.MediaBrowserController
import io.github.vladimirmi.radius.model.repository.StationRepository
import io.github.vladimirmi.radius.navigation.Router
import io.github.vladimirmi.radius.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
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

        repository.selected
                .subscribeBy {
                    viewState.setMedia(it)
                    viewState.createMode(repository.newStation == it)
                    if (browserController.playbackState.value?.state == PlaybackStateCompat.STATE_PLAYING) {
                        browserController.play(it)
                    }
                }
                .addTo(compDisp)
    }

    private fun handleState(state: PlaybackStateCompat?) {
        when (state?.state) {
            STATE_PAUSED, STATE_STOPPED -> viewState.showStopped()
            STATE_PLAYING -> viewState.showPlaying()
        }
    }

    fun playPause() {
        val station = repository.selected.value ?: return
        if (browserController.isPlaying(station)) {
            browserController.stop()
        } else {
            browserController.play(station)
        }
    }

    fun switchFavorite() {
        val selected = repository.selected.value ?: return
        val copy = selected.copy(favorite = !selected.favorite)
        repository.update(copy)
        viewState.setMedia(copy)
    }

    fun showStation() {
        router.showStation(repository.selected.value)
    }

    fun saveBitmap(drawingCache: Bitmap) {
        repository.iconBitmap = drawingCache
    }

    fun skipPrevious() {
        val previous = repository.previous()
        router.skipPrevious(previous)
    }

    fun skipNext() {
        val next = repository.next()
        router.skipNext(next)
    }
}