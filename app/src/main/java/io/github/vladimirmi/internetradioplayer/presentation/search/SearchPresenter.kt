package io.github.vladimirmi.internetradioplayer.presentation.search

import io.github.vladimirmi.internetradioplayer.domain.interactor.SearchInteractor
import io.github.vladimirmi.internetradioplayer.extensions.ioToMain
import io.github.vladimirmi.internetradioplayer.extensions.subscribeByEx
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class SearchPresenter
@Inject constructor(private val searchInteractor: SearchInteractor)
    : BasePresenter<SearchView>() {

    fun search(query: String) {
        searchInteractor.saveQuery(query)
                .ioToMain()
                .subscribe()
                .addTo(dataSubs)
    }

    fun querySuggestions(query: String) {
        searchInteractor.querySuggestions(query)
                .ioToMain()
                .subscribeByEx { view?.setSuggestions(it) }
                .addTo(viewSubs)
    }
}
