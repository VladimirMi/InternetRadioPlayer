package io.github.vladimirmi.radius.presentation.metadata

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.model.repository.MediaController
import io.github.vladimirmi.radius.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 08.12.2017.
 */

@InjectViewState
class MetadataPresenter
@Inject constructor(private val mediaController: MediaController)
    : BasePresenter<MetadataView>() {

    override fun onFirstViewAttach() {
        mediaController.playbackMetaData
                .subscribeBy { handleMeta(it) }
                .addTo(compDisp)

        mediaController.playbackState
                .subscribeBy { handleState(it) }
                .addTo(compDisp)
    }

    private fun handleMeta(meta: MediaMetadataCompat) {
        val metadata = with(meta.description) { "$subtitle - $title" }
        viewState.setMetadata(metadata)
    }

    private fun handleState(state: PlaybackStateCompat) {
        when (state.state) {
            PlaybackStateCompat.STATE_BUFFERING -> viewState.setMetadata(R.string.metadata_buffering)
            PlaybackStateCompat.STATE_PAUSED -> viewState.tryHide()
            PlaybackStateCompat.STATE_STOPPED -> viewState.hide()
        }
    }
}