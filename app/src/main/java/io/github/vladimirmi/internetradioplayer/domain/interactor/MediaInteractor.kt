package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.FavoritesRepository
import io.github.vladimirmi.internetradioplayer.data.repository.MediaRepository
import io.github.vladimirmi.internetradioplayer.data.repository.PlayerRepository
import io.github.vladimirmi.internetradioplayer.data.repository.RecordsRepository
import io.github.vladimirmi.internetradioplayer.data.service.COMMAND_DISABLE_SEEK
import io.github.vladimirmi.internetradioplayer.data.service.COMMAND_DISABLE_SKIP
import io.github.vladimirmi.internetradioplayer.data.service.COMMAND_ENABLE_SEEK
import io.github.vladimirmi.internetradioplayer.data.service.COMMAND_ENABLE_SKIP
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
                    private val recordsRepository: RecordsRepository,
                    private val playerRepository: PlayerRepository) {

    val currentMediaObs: Observable<Media> get() = mediaRepository.currentMediaObs

    val currentStationObs: Observable<Station>
        get() = currentMediaObs
                .filter { it is Station }
                .cast(Station::class.java)

    var currentMedia: Media
        get() = mediaRepository.currentMedia
        set(value) {
            when (value) {
                is Record -> setRecord(value)
                is Station -> setStation(value)
            }
            playerRepository.sendCommand(
                    if (mediaRepository.mediaQueue.queueSize > 1) COMMAND_ENABLE_SKIP
                    else COMMAND_DISABLE_SKIP
            )
        }

    private fun setRecord(record: Record) {
        mediaRepository.mediaQueue = RecordsQueue(recordsRepository.records)
        mediaRepository.currentMedia = record
        playerRepository.sendCommand(COMMAND_ENABLE_SEEK)
    }

    private fun setStation(station: Station) {
        //todo refactor (favorite field)
        val queue = if (favoritesRepository.getStation { it.id == station.id } != null) {
            favoritesRepository.stations
        } else {
            playerRepository.sendCommand(COMMAND_DISABLE_SKIP)
            SingletonMediaQueue(station)
        }
        mediaRepository.mediaQueue = queue
        mediaRepository.currentMedia = station
        playerRepository.sendCommand(COMMAND_DISABLE_SEEK)
    }

    fun nextMedia() {
        mediaRepository.currentMedia = mediaRepository.getNext(currentMedia.id)
    }

    fun previousMedia() {
        mediaRepository.currentMedia = mediaRepository.getPrevious(currentMedia.id)
    }
}