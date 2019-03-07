package io.github.vladimirmi.internetradioplayer.data.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.domain.model.MediaQueue
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 16.02.2019.
 */

class MediaRepository
@Inject constructor() {

    lateinit var mediaQueue: MediaQueue

    val currentMediaObs = BehaviorRelay.createDefault(Media.nullObj())

    var currentMedia: Media
        get() = currentMediaObs.value ?: Media.nullObj()
        set(value) {
            currentMediaObs.accept(value)
        }

    fun getNext(id: String): Media {
        return mediaQueue.getNext(id)
    }

    fun getPrevious(id: String): Media {
        return mediaQueue.getPrevious(id)
    }
}