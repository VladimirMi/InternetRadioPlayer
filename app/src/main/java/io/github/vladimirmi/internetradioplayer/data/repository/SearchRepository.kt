package io.github.vladimirmi.internetradioplayer.data.repository

import io.github.vladimirmi.internetradioplayer.data.db.SuggestionsDatabase
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.db.entity.SuggestionEntity
import io.github.vladimirmi.internetradioplayer.data.net.UberStationsService
import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.data.utils.StationParser
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.reactivex.Completable
import io.reactivex.Observable
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

    fun searchStations(query: String): Single<List<StationSearchRes>> {
        return uberStationsService.searchStations(query)
                .map { it.result }
                .subscribeOn(Schedulers.io())
    }

    fun findUberStation(id: Int): Observable<Station> {
        return uberStationsService.getStation(id)
                .map { it.result[0].stations[0].toStation() }
                .flatMapObservable {
                    Observable.fromCallable {
                        parser.parseFromUberStation(it)
                    }.startWith(it)
                }
                .subscribeOn(Schedulers.io())

    }
}
