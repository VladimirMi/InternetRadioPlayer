package io.github.vladimirmi.radius.model.repository

import io.github.vladimirmi.radius.model.data.MediaSource
import io.github.vladimirmi.radius.model.entity.Media
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class MediaRepository
@Inject constructor(private val mediaSource: MediaSource) {

    fun getMediaList(): List<Media> = mediaSource.mediaList
}