package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.MediaRepository
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 16.02.2019.
 */

class MediaInteractor
@Inject constructor(private val mediaRepository: MediaRepository) {

    val currentMediaObs: Observable<Media> get() = mediaRepository.currentMediaObs

    val currentStationObs: Observable<Station>
        get() = currentMediaObs
                .filter { it is Station }
                .cast(Station::class.java)

    val currentRecordObs: Observable<Record>
        get() = currentMediaObs
                .filter { it is Record }
                .cast(Record::class.java)

    var currentMedia: Media
        get() = mediaRepository.currentMedia
        set(value) {
            mediaRepository.currentMedia = value
        }
}