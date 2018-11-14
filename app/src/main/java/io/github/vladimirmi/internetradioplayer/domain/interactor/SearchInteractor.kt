package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.data.repository.SearchRepository
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchInteractor
@Inject constructor(private val searchRepository: SearchRepository) {

    fun saveQuery(query: String): Completable {
        return searchRepository.saveQuery(query)
                .subscribeOn(Schedulers.io())
    }

    fun queryRecentSuggestions(query: String): Single<List<Suggestion>> {
        return searchRepository.getRecentSuggestions(query)
                .map { it.asReversed() }
                .subscribeOn(Schedulers.io())
    }

    fun queryRegularSuggestions(query: String): Single<List<Suggestion>> {
        return searchRepository.getRegularSuggestions(query)
                .subscribeOn(Schedulers.io())
    }
}
