package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.MediaRepository
import io.github.vladimirmi.internetradioplayer.data.repository.RecordsRepository
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 15.02.2019.
 */

class RecordsInteractor
@Inject constructor(private val recordsRepository: RecordsRepository,
                    private val mediaRepository: MediaRepository) {

    val recordsObs: Observable<List<Record>> get() = recordsRepository.recordsObs

    val sortedRecordsObs: Observable<List<Record>>
        get() = recordsRepository.recordsObs
                .map { records -> records.sortedBy(Record::createdAt) }

    fun deleteRecord(record: Record): Completable {
        return recordsRepository.deleteRecord(record)
    }

    fun startRecordingCurrentStation() {
        val station = mediaRepository.currentMedia as? Station ?: return
        recordsRepository.startRecording(station)
    }

    fun isCurrentRecordingObs(): Observable<Boolean> {
        return Observables.combineLatest(recordsRepository.currentRecordingObs,
                mediaRepository.currentMediaObs) { set: Set<String>, media: Media ->
            set.contains(media.id)
        }.distinctUntilChanged()
    }
}