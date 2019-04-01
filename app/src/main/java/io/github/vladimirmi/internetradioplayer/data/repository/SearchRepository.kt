package io.github.vladimirmi.internetradioplayer.data.repository

import io.github.vladimirmi.internetradioplayer.data.db.SuggestionsDatabase
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.db.entity.SuggestionEntity
import io.github.vladimirmi.internetradioplayer.data.net.UberStationsService
import io.github.vladimirmi.internetradioplayer.data.net.ubermodel.*
import io.github.vladimirmi.internetradioplayer.data.utils.StationParser
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchRepository
@Inject constructor(db: SuggestionsDatabase,
                    private val uberStationsService: UberStationsService,
                    private val parser: StationParser) {

    private val dao = db.suggestionsDao()

    fun saveQuery(query: String): Completable {
        return Completable.fromCallable {
            dao.insert(SuggestionEntity(query, System.currentTimeMillis()))
        }.subscribeOn(Schedulers.io())
    }

    fun getRecentSuggestions(query: String): Single<out List<Suggestion>> {
        return dao.getSuggestions("%$query%")
                .map { list -> list.map { Suggestion.Recent(it.value) } }
                .subscribeOn(Schedulers.io())
    }

    fun getRegularSuggestions(query: String): Single<out List<Suggestion>> {
        return uberStationsService.getSuggestions("*$query*")
                .map { suggestions -> suggestions.result.map { Suggestion.Regular(it.keyword) } }
                .subscribeOn(Schedulers.io())
    }

    fun deleteRecentSuggestion(suggestion: Suggestion): Completable {
        return dao.getSuggestion(suggestion.value)
                .flatMapCompletable { Completable.fromAction { dao.delete(it) } }
                .subscribeOn(Schedulers.io())
    }

    fun searchStations(query: String): Single<List<StationResult>> {
        return uberStationsService.searchStations(query)
                .map { it.result }
                .subscribeOn(Schedulers.io())
    }

    fun searchStation(id: Int): Single<StationIdResult> {
        return uberStationsService.getStation(id)
                .map(StationIdSearch::getResult)
                .subscribeOn(Schedulers.io())
    }

    fun parseFromNet(station: Station): Single<Station> {
        return Single.fromCallable { parser.parseFromStation(station) }
                .subscribeOn(Schedulers.io())
    }

    fun searchTopSongs(query: String): Single<List<TopSongResult>> {
        return uberStationsService.searchTopSongs(query)
                .map { it.result }
                .subscribeOn(Schedulers.io())
    }

    fun searchTalks(query: String): Single<List<TalkResult>> {
        return uberStationsService.getTalks()
                .map { search -> search.result.filter { it.genre == query } }
                .subscribeOn(Schedulers.io())
    }

    fun searchTalk(id: String): Single<TalkIdResult> {
        return uberStationsService.searchTalk(id)
                .map { it.result.first() }
                .subscribeOn(Schedulers.io())
    }
}
