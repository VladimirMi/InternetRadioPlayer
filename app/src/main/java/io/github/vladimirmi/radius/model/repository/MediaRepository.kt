package io.github.vladimirmi.radius.model.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import io.github.vladimirmi.radius.model.entity.Media
import io.github.vladimirmi.radius.model.source.MediaSource
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class MediaRepository
@Inject constructor(private val mediaSource: MediaSource) {
    val mediaListData: LiveData<List<Media>> = MutableLiveData()
    val selectedData: LiveData<Media> = MutableLiveData()

    fun initMedia() {
        setMediaList(mediaSource.getMediaList())
        setSelected(mediaListData.value?.firstOrNull())
    }

    fun setMediaList(mediaList: List<Media>) {
        (mediaListData as MutableLiveData).value = mediaList
    }

    fun setSelected(media: Media?) {
        (selectedData as MutableLiveData).value = media
    }

    fun updateAndSave(media: Media) {
        update(media)
        save(media)
    }

    fun update(media: Media) {
        val list = mediaListData.value as ArrayList
        list[indexOfFirst(media)] = media
        setMediaList(list)
    }

    private fun save(media: Media) {
        mediaSource.save(media.path, media)
    }

    private fun indexOfFirst(media: Media): Int {
        return mediaListData.value?.indexOfFirst { it.path == media.path } ?: return -1
    }
}