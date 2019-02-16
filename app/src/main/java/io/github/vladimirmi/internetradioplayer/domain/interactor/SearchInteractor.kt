package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.data.repository.FavoritesRepository
import io.github.vladimirmi.internetradioplayer.data.repository.MediaRepository
import io.github.vladimirmi.internetradioplayer.data.repository.SearchRepository
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchInteractor
@Inject constructor(private val searchRepository: SearchRepository,
                    private val favoritesRepository: FavoritesRepository,
                    private val mediaRepository: MediaRepository) {

    private var suggestions: List<Suggestion> = emptyList()

    fun queryRecentSuggestions(query: String): Single<out List<Suggestion>> {
        return searchRepository.getRecentSuggestions(query.trim())
    }

    fun queryRegularSuggestions(query: String): Observable<out List<Suggestion>> {
        suggestions = suggestions.filter { it.value.contains(query, true) || query.contains(it.value, true) }

        return Observable.concat(Observable.just(suggestions),
                searchRepository.getRegularSuggestions(query.trim())
                        .delaySubscription(500, TimeUnit.MILLISECONDS)
                        .doOnSuccess { suggestions = it }
                        .toObservable())
    }

    fun searchStations(query: String): Single<List<StationSearchRes>> {
        return searchRepository.saveQuery(query)
                .andThen(searchRepository.searchStations(query))
    }

    fun selectUberStation(id: Int): Completable {
        return searchRepository.findUberStation(id)
                .doOnNext { station ->
                    mediaRepository.currentMedia = favoritesRepository.getStation { it.uri == station.uri }
                            ?: station
                }.ignoreElements()
    }
}
