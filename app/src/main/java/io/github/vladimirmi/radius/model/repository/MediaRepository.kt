package io.github.vladimirmi.radius.model.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.net.Uri
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
    val groupedMediaList: GroupedList<Media> get() = mediaList

    fun initMedia() {
        mediaList = GroupingMedia(mediaSource.getMediaList())
        (groupedMediaData as MutableLiveData).value = mediaList
        if (mediaList.size > preferences.selectedPos) {
            setSelected(preferences.selectedPos)
        }
    }

    fun setSelected(media: Media) {
        val pos = indexOfFirst(media)
        (selectedData as MutableLiveData).value = pos
        preferences.selectedPos = pos
    }

    fun getSelected(): Media? = selectedData.value?.let { mediaList[it] }

    fun updateAndSave(media: Media) {
        update(media)
        save(media)
    }

    private fun update(media: Media) {
        mediaList[indexOfFirst(media)] = media
        (groupedMediaData as MutableLiveData).value = mediaList
    }

    private fun save(media: Media) {
        mediaSource.save(media)
    }

    private fun setSelected(pos: Int) {
        (selectedData as MutableLiveData).value = pos
    }

    private fun indexOfFirst(media: Media): Int {
        return mediaList.indexOfFirst { it.path == media.path }
    }

    fun addMedia(uri: Uri, cb: (Media?) -> Unit) {
        mediaSource.fromUri(uri) { media ->
            if (media != null) {
                setSelected(media)
                if (mediaList.find { it.path == media.path } != null) return@fromUri
                mediaList.add(media)
                (groupedMediaData as MutableLiveData).value = mediaList
            }
            cb(media)
        }
    }
}
