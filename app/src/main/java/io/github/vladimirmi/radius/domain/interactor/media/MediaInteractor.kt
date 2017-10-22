package io.github.vladimirmi.radius.domain.interactor.media

import io.github.vladimirmi.radius.data.entity.Media
import io.github.vladimirmi.radius.data.repository.MediaRepository
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class MediaInteractor
@Inject constructor(private val mediaRepository: MediaRepository) {

    fun getMediaList(): List<Media> = mediaRepository.getMediaList()
}