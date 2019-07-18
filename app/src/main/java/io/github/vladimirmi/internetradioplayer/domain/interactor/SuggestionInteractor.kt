package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.repository.SuggestionRepository
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 06.04.2019.
 */

class SuggestionInteractor
@Inject constructor(private val suggestionRepository: SuggestionRepository) {

    private var suggestions: List<Suggestion> = emptyList()

    fun queryRecentSuggestions(query: String): Single<out List<Suggestion>> {
        return suggestionRepository.getRecentSuggestions(query.trim())
    }

    fun queryRegularSuggestions(query: String): Observable<out List<Suggestion>> {
        suggestions = suggestions.filter { it.value.contains(query, true) || query.contains(it.value, true) }

        return Observable.concat(Observable.just(suggestions),
                suggestionRepository.getRegularSuggestions(query.trim())
                        .delaySubscription(500, TimeUnit.MILLISECONDS)
                        .doOnSuccess { suggestions = it }
                        .toObservable())
    }

    fun deleteRecentSuggestion(suggestion: Suggestion): Completable {
        return suggestionRepository.deleteRecentSuggestion(suggestion)
    }

    fun saveSuggestion(query: String): Completable {
        return suggestionRepository.saveSuggestion(query)
    }
}