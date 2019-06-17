package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.repository.FavoritesRepository
import io.github.vladimirmi.internetradioplayer.data.repository.HistoryRepository
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 02.12.2018.
 */

class HistoryInteractor
@Inject constructor(private val historyRepository: HistoryRepository,
                    private val favoritesRepository: FavoritesRepository,
                    private val mediaInteractor: MediaInteractor) {

    fun initHistory(): Completable {
        return getHistoryObs()
                .first(emptyList())
                .doOnSuccess { stations ->
                    stations.find { mediaInteractor.getSavedMediaId() == it.id }
                            ?.let { mediaInteractor.currentMedia = it }
                }.ignoreElement()
    }

    fun getHistoryObs(): Observable<List<Station>> {
        return Observables.combineLatest(historyRepository.getHistoryObs(),
                favoritesRepository.stationsListObs) { history, _ ->
            history.map { station ->
                favoritesRepository.getStation { it.uri == station.uri }
                        ?: station.apply { isFavorite = false }
            }
        }
    }

    fun createHistory(station: Station) {
        historyRepository.createHistory(station).subscribeX()
    }

    fun deleteHistory(station: Station): Completable {
        return historyRepository.deleteHistory(station.id)
    }
}