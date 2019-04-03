package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.net.ubermodel.StationResult
import io.github.vladimirmi.internetradioplayer.data.net.ubermodel.TalkResult
import io.github.vladimirmi.internetradioplayer.data.net.ubermodel.TopSongResult
import io.github.vladimirmi.internetradioplayer.data.repository.FavoritesRepository
import io.github.vladimirmi.internetradioplayer.data.repository.SearchRepository
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.github.vladimirmi.internetradioplayer.domain.model.Talk
import io.github.vladimirmi.internetradioplayer.utils.MessageException
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
                    private val mediaInteractor: MediaInteractor) {

    //todo the suggestions interactor and repo
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

    fun deleteRecentSuggestion(suggestion: Suggestion): Completable {
        return searchRepository.deleteRecentSuggestion(suggestion)
    }

    fun searchStations(query: String): Single<List<Media>> {
        return searchRepository.saveQuery(query)
                .andThen(searchRepository.searchStations(query))
                .map { list -> list.map(StationResult::toStation) }
    }

    fun searchTopSongs(query: String): Single<List<Media>> {
        return searchRepository.searchTopSongs(query)
                .map { list -> list.map(TopSongResult::toStation) }
    }

    fun searchTalks(query: String): Single<List<Media>> {
        return searchRepository.searchTalks(query)
                .map { list -> list.map(TalkResult::toTalk) }
    }

    fun selectMedia(media: Media): Completable {
        return when (media) {
            is Station -> selectStation(media)
            is Talk -> selectTalk(media)
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

    private fun selectTalk(talk: Talk): Completable {
        return searchRepository.searchTalk(talk.remoteId)
                .flatMapCompletable {
                    if (it.uri.isBlank()) {
                        Completable.error(MessageException("No stations currently airing this show"))
                    } else {
                        mediaInteractor.setMedia(it.toTalk(talk))
                    }
                }
    }
}
