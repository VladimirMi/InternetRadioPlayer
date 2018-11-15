package io.github.vladimirmi.internetradioplayer.domain.interactor

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.data.repository.SearchRepository
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.github.vladimirmi.internetradioplayer.extensions.MessageResException
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchInteractor
@Inject constructor(private val searchRepository: SearchRepository) {

    fun queryRecentSuggestions(query: String): Single<List<Suggestion>> {
        return searchRepository.getRecentSuggestions(query)
                .map { it.asReversed() }
                .subscribeOn(Schedulers.io())
    }

    fun queryRegularSuggestions(query: String): Single<List<Suggestion>> {
        return searchRepository.getRegularSuggestions(query)
                .subscribeOn(Schedulers.io())
    }

    fun searchStations(query: String): Single<List<StationSearchRes>> {
        //todo change string
        if (query.length < 3) return Single.error(MessageResException(R.string.toast_name_empty_error))

        return searchRepository.saveQuery(query)
                .andThen(searchRepository.searchStations(query))
                .subscribeOn(Schedulers.io())
    }
}
