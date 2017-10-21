package io.github.vladimirmi.radius.model.repository

import android.net.Uri
import io.github.vladimirmi.radius.model.data.MediaSource
import io.github.vladimirmi.radius.model.entity.Media
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class MediaRepository
@Inject constructor(private val mediaSource: MediaSource) {

    fun getMediaList(): List<Media> = mediaSource.mediaList
    fun currentMedia(): Media? = mediaSource.currentMedia
    fun currentMedia(uri: Uri) {
        mediaSource.currentMedia = mediaSource.mediaList.find { it.uri == uri }!!
    }
}