package io.github.vladimirmi.radius.presentation.media

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.github.vladimirmi.radius.model.entity.Media
import io.github.vladimirmi.radius.model.repository.MediaRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

@InjectViewState
class MediaPresenter
@Inject constructor(private val mediaRepository: MediaRepository)
    : MvpPresenter<MediaView>() {

    override fun onFirstViewAttach() {
        mediaRepository.initMedia()
        viewState.setMediaList(mediaRepository.mediaListData)
    }

    fun select(media: Media) {
        Timber.e("select: ${media.uri}")
        mediaRepository.selectedMediaData.value = media
    }
}


