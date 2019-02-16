package io.github.vladimirmi.internetradioplayer.data.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 16.02.2019.
 */

class MediaRepository
@Inject constructor() {

    val currentMediaObs = BehaviorRelay.create<Media>()

    var currentMedia: Media
        get() = currentMediaObs.value ?: Station.nullObj()
        set(value) {
            currentMediaObs.accept(value)
        }
}