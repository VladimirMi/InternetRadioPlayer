package io.github.vladimirmi.internetradioplayer.data.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.github.vladimirmi.internetradioplayer.domain.model.MediaInfo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 01.04.2019.
 */

class MediaInfoRepository
@Inject constructor() {

    val currentMediaInfo = BehaviorRelay.createDefault(MediaInfo.nullObj())
}