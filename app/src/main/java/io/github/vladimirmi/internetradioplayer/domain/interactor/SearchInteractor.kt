package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.data.repository.FavoritesRepository
import io.github.vladimirmi.internetradioplayer.data.repository.SearchRepository
import io.github.vladimirmi.internetradioplayer.data.repository.StationRepository
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchInteractor
@Inject constructor(private val searchRepository: SearchRepository,
                    private val stationRepository: StationRepository,
                    private val favoritesRepository: FavoritesRepository) {

    fun queryRecentSuggestions(query: String): Single<out List<Suggestion>> {
        return searchRepository.getRecentSuggestions(query.trim())
                .map { it.asReversed() }
    }

    fun queryRegularSuggestions(query: String): Single<out List<Suggestion>> {
        return searchRepository.getRegularSuggestions(query.trim())
    }

    fun searchStations(query: String): Single<List<StationSearchRes>> {
        return searchRepository.saveQuery(query)
                .andThen(searchRepository.searchStations(query))
    }

    fun selectUberStation(id: Int): Completable {
        return searchRepository.findUberStation(id)
                .doOnNext { station ->
                    val newStation = favoritesRepository.stations.findStation { it.uri == station.uri }
                            ?: station
                    stationRepository.station = newStation
                }.ignoreElements()
    }
}
