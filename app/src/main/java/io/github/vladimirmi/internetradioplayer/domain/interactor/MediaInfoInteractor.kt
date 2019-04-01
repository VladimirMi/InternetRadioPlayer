package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.repository.MediaInfoRepository
import io.github.vladimirmi.internetradioplayer.domain.model.MediaInfo
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 01.04.2019.
 */

class MediaInfoInteractor
@Inject constructor(private val mediaInfoRepository: MediaInfoRepository) {

    val currentMediaInfo: Observable<MediaInfo>
        get() = mediaInfoRepository.currentMediaInfo


    fun setMediaInfo(info: MediaInfo) {
        mediaInfoRepository.currentMediaInfo.accept(info)
    }

    fun updateMediaInfo(info: MediaInfo) {
        val mediaInfo = mediaInfoRepository.currentMediaInfo.value!!.update(info)
        setMediaInfo(mediaInfo)
    }
}