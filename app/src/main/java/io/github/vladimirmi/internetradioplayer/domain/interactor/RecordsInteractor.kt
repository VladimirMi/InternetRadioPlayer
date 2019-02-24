package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.repository.RecordsRepository
import io.github.vladimirmi.internetradioplayer.domain.model.Record
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 15.02.2019.
 */

class RecordsInteractor
@Inject constructor(private val recordsRepository: RecordsRepository) {

    val recordsObs: Observable<List<Record>> get() = recordsRepository.recordsObs

    fun deleteRecord(record: Record): Completable {
        return recordsRepository.deleteRecord(record)
    }
}