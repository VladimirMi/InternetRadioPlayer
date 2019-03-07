package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.FavoritesRepository
import io.github.vladimirmi.internetradioplayer.data.repository.MediaRepository
import io.github.vladimirmi.internetradioplayer.data.repository.RecordsRepository
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.github.vladimirmi.internetradioplayer.domain.model.RecordsQueue
import io.github.vladimirmi.internetradioplayer.domain.model.SingletonMediaQueue
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 16.02.2019.
 */

class MediaInteractor
@Inject constructor(private val mediaRepository: MediaRepository,
                    private val favoritesRepository: FavoritesRepository,
                    private val recordsRepository: RecordsRepository) {

    val currentMediaObs: Observable<Media> get() = mediaRepository.currentMediaObs

    val currentStationObs: Observable<Station>
        get() = currentMediaObs
                .filter { it is Station }
                .cast(Station::class.java)

    var currentMedia: Media
        get() = mediaRepository.currentMedia
        set(value) {
            when {
                value is Record -> {
                    mediaRepository.mediaQueue = RecordsQueue(recordsRepository.records)
                }
                //todo refactor (favorite field)
                favoritesRepository.getStation { it.id == value.id } != null -> mediaRepository.mediaQueue = favoritesRepository.stations
                else -> mediaRepository.mediaQueue = SingletonMediaQueue(value)
            }
            mediaRepository.currentMedia = value
        }

    fun nextMedia() {
        mediaRepository.currentMedia = mediaRepository.getNext(currentMedia.id)
    }

    fun previousMedia() {
        mediaRepository.currentMedia = mediaRepository.getPrevious(currentMedia.id)
    }
}