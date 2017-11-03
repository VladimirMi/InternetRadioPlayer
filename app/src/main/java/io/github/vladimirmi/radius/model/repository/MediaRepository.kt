package io.github.vladimirmi.radius.model.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import io.github.vladimirmi.radius.model.entity.GroupedList
import io.github.vladimirmi.radius.model.entity.GroupingMedia
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
    private lateinit var mediaList: GroupingMedia
    val selectedData: LiveData<Int> = MutableLiveData()
    val groupedMediaData: LiveData<GroupedList<Media>> = MutableLiveData()

    fun initMedia() {
        mediaList = GroupingMedia(mediaSource.getMediaList())
        setGrouped(mediaList)
        if (mediaList.isNotEmpty()) {
            setSelected(preferences.selectedPos)
        }
    }

    fun setSelected(media: Media) {
        val pos = indexOfFirst(media)
        (selectedData as MutableLiveData).value = pos
        preferences.selectedPos = pos
    }

    fun getSelected(): Media? = selectedData.value?.let { mediaList[it] }

    fun setGrouped(grouping: GroupingMedia) {
        (groupedMediaData as MutableLiveData).value = grouping
    }

    fun getGrouped(): GroupedList<Media> = mediaList

    fun updateAndSave(media: Media) {
        update(media)
        save(media)
    }

    private fun update(media: Media) {
        mediaList[indexOfFirst(media)] = media
        setGrouped(mediaList)
    }

    private fun save(media: Media) {
        mediaSource.save(media)
    }

    private fun setSelected(pos: Int) {
        (selectedData as MutableLiveData).value = pos
    }

    private fun indexOfFirst(media: Media): Int {
        return mediaList.indexOfFirst { it.id == media.id }
    }
}
