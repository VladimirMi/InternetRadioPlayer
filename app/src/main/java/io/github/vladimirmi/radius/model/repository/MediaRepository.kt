package io.github.vladimirmi.radius.model.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import io.github.vladimirmi.radius.model.entity.Media
import io.github.vladimirmi.radius.model.manager.Preferences
import io.github.vladimirmi.radius.model.source.MediaSource
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class MediaRepository
@Inject constructor(private val mediaSource: MediaSource,
                    private val preferences: Preferences) {
    val mediaListData: LiveData<List<Media>> = MutableLiveData()
    val selectedPosData: LiveData<Int> = MutableLiveData()

    fun initMedia() {
        val mediaList = mediaSource.getMediaList()
        setMediaList(mediaList)
        if (mediaList.isNotEmpty()) {
            setSelected(preferences.selectedPos)
        }
    }

    private fun setMediaList(mediaList: List<Media>) {
        (mediaListData as MutableLiveData).value = mediaList
    }

    fun setSelected(media: Media) {
        val pos = indexOfFirst(media)
        (selectedPosData as MutableLiveData).value = pos
        preferences.selectedPos = pos
    }

    fun getSelected(): Media? {
        return selectedPosData.value?.let { mediaListData.value?.get(it) }
    }

    fun updateAndSave(media: Media) {
        update(media)
        save(media)
    }

    private fun update(media: Media) {
        val list = mediaListData.value as ArrayList
        list[indexOfFirst(media)] = media
        setMediaList(list)
    }

    private fun save(media: Media) {
        mediaSource.save(media)
    }

    private fun setSelected(pos: Int) {
        (selectedPosData as MutableLiveData).value = pos
    }

    private fun indexOfFirst(media: Media): Int {
        return mediaListData.value?.indexOfFirst { it.uri == media.uri } ?: return -1
    }
}
