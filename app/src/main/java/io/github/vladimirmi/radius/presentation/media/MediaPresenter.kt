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
@Inject constructor(private val mediaRepository: MediaRepository,
                    private val mediaBrowserController: MediaBrowserController)
    : BasePresenter<MediaView>() {

    override fun onFirstAttach() {
        mediaRepository.initMedia()
        viewState.setMediaList(mediaRepository.mediaListData)
        mediaBrowserController.playbackState.observe(this, Observer {
            val media = mediaRepository.selectedMediaData.value ?: return@Observer
            if (it?.state == PlaybackStateCompat.STATE_PLAYING) {
                viewState.select(media, playing = true)
            } else {
                viewState.select(media, playing = false)
            }
        })
    }

    fun select(media: Media) {
        mediaRepository.selectedMediaData.value?.let { viewState.unselect(it) }
        mediaRepository.selectedMediaData.value = media
    }
}


