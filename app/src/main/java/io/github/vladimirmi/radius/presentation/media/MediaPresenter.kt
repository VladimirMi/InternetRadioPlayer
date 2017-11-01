package io.github.vladimirmi.radius.presentation.media

import android.arch.lifecycle.Observer
import android.support.v4.media.session.PlaybackStateCompat
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.model.entity.Media
import io.github.vladimirmi.radius.model.repository.MediaBrowserController
import io.github.vladimirmi.radius.model.repository.MediaRepository
import io.github.vladimirmi.radius.ui.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

@InjectViewState
class MediaPresenter
@Inject constructor(private val repository: MediaRepository,
                    private val mediaBrowserController: MediaBrowserController)
    : BasePresenter<MediaView>() {

    override fun onFirstAttach() {
        repository.mediaListData.observe(this, Observer {
            it?.let { viewState.setMediaList(it) }
        })

        repository.selectedData.observe(this, Observer {
            it?.let { viewState.select(it, playing = false) }
        })

        mediaBrowserController.playbackState.observe(this, Observer {
            if (it?.state == PlaybackStateCompat.STATE_PLAYING) {
                repository.selectedData.value?.let { viewState.select(it, playing = true) }
            } else {
                repository.selectedData.value?.let { viewState.select(it, playing = false) }
            }
        })
    }

    fun select(media: Media) {
        repository.setSelected(media)
    }
}


