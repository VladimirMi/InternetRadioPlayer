package io.github.vladimirmi.radius.model.repository

import android.arch.lifecycle.MutableLiveData
import io.github.vladimirmi.radius.model.entity.Media
import io.github.vladimirmi.radius.model.source.MediaSource
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class MediaRepository
@Inject constructor(private val mediaSource: MediaSource) {
    val mediaListData: MutableLiveData<List<Media>> = MutableLiveData()
    val selectedMediaData: MutableLiveData<Media> = MutableLiveData()

    fun initMedia() {
        mediaListData.value = mediaSource.getMediaList()
        selectedMediaData.value = mediaListData.value?.firstOrNull()
    }
}