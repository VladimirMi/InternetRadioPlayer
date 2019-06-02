package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.net.UberStationsService
import io.github.vladimirmi.internetradioplayer.data.repository.FavoritesRepository
import io.github.vladimirmi.internetradioplayer.data.repository.SearchRepository
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.domain.model.SearchState
import io.github.vladimirmi.internetradioplayer.utils.MessageResException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchInteractor
@Inject constructor(private val searchRepository: SearchRepository,
                    private val favoritesRepository: FavoritesRepository,
                    private val mediaInteractor: MediaInteractor) {

    fun search(endpoint: String?, query: String?): Observable<SearchState> {
        if (query == null) return Observable.just(SearchState.Data(emptyList()))
        val q = query.trim()
        return when (endpoint) {
            UberStationsService.STATIONS_ENDPOINT -> searchStations(q)
            UberStationsService.TOPSONGS_ENDPOINT -> searchTopSongs(q)
            else -> Observable.error(IllegalStateException("Can't find endpoint $endpoint"))
        }
    }

    private fun searchStations(query: String): Observable<SearchState> {
        if (query.length < 3) return Observable.just(SearchState.Error(MessageResException(R.string.msg_text_short)))
        return searchStationsWithFavorites(searchRepository.searchStations(query)) {
            it.toStation()
        }
    }

    private fun searchTopSongs(query: String): Observable<SearchState> {
        return searchStationsWithFavorites(searchRepository.searchTopSongs(query.trim())) {
            it.toStation()
        }
    }

    fun selectMedia(media: Media): Completable {
        return when (media) {
            is Station -> selectStation(media)
            else -> Completable.complete()
        }
    }

    private fun selectStation(station: Station): Completable {
        return searchRepository.searchStation(station.remoteId)
                .flatMapObservable { response ->
                    val newStation = response.toStation()
                    searchRepository.parseFromNet(newStation)
                            .toObservable()
                            .startWith(newStation)
                }
                .doOnNext { newStation ->
                    mediaInteractor.currentMedia =
                            favoritesRepository.getStation { it.uri == newStation.uri }
                                    ?: newStation
                }.ignoreElements()
    }

    private fun <T> searchStationsWithFavorites(searchObs: Single<List<T>>, transform: (T) -> Station)
            : Observable<SearchState> {
        return Observables.combineLatest(favoritesRepository.stationsListObs, searchObs.toObservable())
        { _, result ->
            val data = result.map { element ->
                val station = transform(element)
                favoritesRepository.getStation { it.uri == station.uri } ?: station
            }
            SearchState.Data(data) as SearchState
        }
                .startWith(SearchState.Loading)
                .onErrorReturn { SearchState.Error(it) }
    }
}
