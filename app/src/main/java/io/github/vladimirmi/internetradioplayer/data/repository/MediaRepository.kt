package io.github.vladimirmi.internetradioplayer.data.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.data.utils.Preferences
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.domain.model.MediaQueue
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 16.02.2019.
 */

class MediaRepository
@Inject constructor(private val prefs: Preferences) {

    lateinit var mediaQueue: MediaQueue

    val currentMediaObs = BehaviorRelay.createDefault(Media.nullObj())

    var currentMedia: Media
        get() = currentMediaObs.value ?: Media.nullObj()
        set(value) {
            prefs.mediaId = value.id
            currentMediaObs.accept(value)
        }

    fun getNext(id: String): Media {
        return mediaQueue.getNext(id) ?: Media.nullObj()
    }

    fun getPrevious(id: String): Media {
        return mediaQueue.getPrevious(id) ?: Media.nullObj()
    }

    fun getSavedMediaId(): String {
        return prefs.mediaId
    }
}