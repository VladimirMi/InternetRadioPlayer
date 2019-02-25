package io.github.vladimirmi.internetradioplayer.data.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 16.02.2019.
 */

class MediaRepository
@Inject constructor() {

    val currentMediaObs = BehaviorRelay.createDefault(Media.nullObj())

    var currentMedia: Media
        get() = currentMediaObs.value ?: Media.nullObj()
        set(value) {
            currentMediaObs.accept(value)
        }
}