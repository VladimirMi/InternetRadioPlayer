package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.data.repository.GroupListRepository
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
                    private val groupListRepository: GroupListRepository) {

    fun queryRecentSuggestions(query: String): Single<out List<Suggestion>> {
        return searchRepository.getRecentSuggestions(query)
                .map { it.asReversed() }
    }

    fun queryRegularSuggestions(query: String): Single<out List<Suggestion>> {
        return searchRepository.getRegularSuggestions(query)
    }

    fun searchStations(query: String): Single<List<StationSearchRes>> {
        return searchRepository.saveQuery(query)
                .andThen(searchRepository.searchStations(query))
    }

    fun selectUberStation(id: Int): Completable {
        return searchRepository.getStation(id)
                .map { it.result[0].stations[0] }
                .doOnSuccess { station ->
                    val newStation: Station = groupListRepository.list.findStation { it.uri == station.uri }
                            ?: station.toStation()
                    stationRepository.station = newStation
                }.ignoreElement()
    }
}
