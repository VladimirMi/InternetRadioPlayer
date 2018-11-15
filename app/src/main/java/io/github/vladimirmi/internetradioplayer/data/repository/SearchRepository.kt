package io.github.vladimirmi.internetradioplayer.data.repository

import io.github.vladimirmi.internetradioplayer.data.db.SuggestionsDatabase
import io.github.vladimirmi.internetradioplayer.data.db.entity.SuggestionEntity
import io.github.vladimirmi.internetradioplayer.data.net.UberStationsService
import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchRepository
@Inject constructor(db: SuggestionsDatabase,
                    private val uberStationsService: UberStationsService) {

    private val dao = db.suggestionsDao()

    fun saveQuery(query: String): Completable {
        return Completable.fromCallable {
            dao.insert(SuggestionEntity(query))
        }
    }

    fun getRecentSuggestions(query: String): Single<List<Suggestion>> {
        return dao.getSuggestions("%$query%")
                .map { list -> list.map { Suggestion.Recent(it.value) } }
    }

    fun getRegularSuggestions(query: String): Single<List<Suggestion>> {
        return uberStationsService.getSuggestions("*$query*")
                .map { suggestions -> suggestions.result.map { Suggestion.Regular(it.keyword) } }
    }

    fun searchStations(query: String): Single<List<StationSearchRes>> {
        return uberStationsService.searchStations(query)
                .map { it.result }
    }
}
