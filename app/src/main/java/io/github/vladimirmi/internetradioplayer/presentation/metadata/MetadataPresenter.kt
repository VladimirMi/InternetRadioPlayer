package io.github.vladimirmi.internetradioplayer.presentation.metadata

import android.support.v4.media.session.PlaybackStateCompat
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.model.entity.Metadata
import io.github.vladimirmi.internetradioplayer.model.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 08.12.2017.
 */

@InjectViewState
class MetadataPresenter
@Inject constructor(private val controlsInteractor: PlayerControlsInteractor)
    : BasePresenter<MetadataView>() {

    override fun onFirstViewAttach() {
        controlsInteractor.playbackMetaData
                .map { Metadata.create(it) }
                .subscribeBy { handleMeta(it) }
                .addTo(compDisp)

        controlsInteractor.playbackState
                .subscribeBy { handleState(it) }
                .addTo(compDisp)
    }

    private fun handleMeta(metadata: Metadata) {
        if (metadata.isUnsupported) {
            viewState.hide()
        } else {
            viewState.setMetadata(metadata.toString())
        }
    }

    private fun handleState(state: PlaybackStateCompat) {
        when (state.state) {
            PlaybackStateCompat.STATE_BUFFERING -> {
                viewState.show()
                viewState.setMetadata(R.string.metadata_buffering)
            }
            PlaybackStateCompat.STATE_PLAYING -> viewState.show()

            PlaybackStateCompat.STATE_PAUSED,
            PlaybackStateCompat.STATE_STOPPED -> viewState.hide()
        }
    }
}