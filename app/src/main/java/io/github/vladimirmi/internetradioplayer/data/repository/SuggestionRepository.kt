package io.github.vladimirmi.internetradioplayer.data.repository

import io.github.vladimirmi.internetradioplayer.data.db.SuggestionsDatabase
import io.github.vladimirmi.internetradioplayer.data.db.entity.SuggestionEntity
import io.github.vladimirmi.internetradioplayer.data.net.UberStationsService
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 06.04.2019.
 */

class SuggestionRepository
@Inject constructor(db: SuggestionsDatabase,
                    private val uberStationsService: UberStationsService) {

    private val dao = db.suggestionsDao()

    fun saveSuggestion(query: String): Completable {
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
}