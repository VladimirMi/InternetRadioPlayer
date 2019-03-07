package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.HistoryRepository
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 02.12.2018.
 */

class HistoryInteractor
@Inject constructor(private val historyRepository: HistoryRepository,
                    private val mediaInteractor: MediaInteractor) {

    fun getHistoryObs(): Observable<List<Station>> {
        return historyRepository.getHistoryObs()
    }

    fun createHistory(station: Station) {
        historyRepository.createHistory(station).subscribeX()
    }

    fun selectRecentStation(): Completable {
        return getHistoryObs()
                .first(emptyList())
                .flatMapCompletable { stations ->
                    Completable.fromAction {
                        stations.firstOrNull()?.let { mediaInteractor.currentMedia = it }
                    }
                }
    }

    fun deleteHistory(station: Station): Completable {
        return historyRepository.deleteHistory(station.id)
    }
}