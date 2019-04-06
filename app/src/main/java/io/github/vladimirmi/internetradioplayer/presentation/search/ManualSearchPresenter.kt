package io.github.vladimirmi.internetradioplayer.presentation.search

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.interactor.MediaInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.SearchInteractor
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.domain.model.Suggestion
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 12.11.2018.
 */

class ManualSearchPresenter
@Inject constructor(private val searchInteractor: SearchInteractor,
                    private val mediaInteractor: MediaInteractor)
    : BasePresenter<ManualSearchView>() {

    var intervalSearchEnabled: Boolean = false
    private var searchSub: Disposable? = null
    private var suggestionSub: Disposable? = null
    private var selectSub: Disposable? = null

    override fun onFirstAttach(view: ManualSearchView) {
        searchInteractor.queryRecentSuggestions("")
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { view.showPlaceholder(it.isEmpty()) }
                .filter { it.isNotEmpty() }
                .map { it.first() }
                .subscribeX(onNext = { view.selectSuggestion(it) })
                .addTo(viewSubs)
    }

    override fun onAttach(view: ManualSearchView) {
        mediaInteractor.currentMediaObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.selectMedia(it.uri) })
                .addTo(viewSubs)
    }

    override fun onDetach() {
        searchSub?.dispose()
        suggestionSub?.dispose()
    }

    fun selectMedia(media: Media) {
        selectSub?.dispose()
        selectSub = searchInteractor.selectMedia(media)
                .subscribeX()
    }

    fun submitSearch(query: String) {
        searchSub?.dispose()

        searchSub = Observable.interval(0, 60, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .map { query.trim() }
                .filter { intervalSearchEnabled && it.isNotEmpty() }
                .doOnNext { if (it.length < 3) view?.showToast(R.string.msg_text_short) }
                .filter { it.length > 2 }
                .doOnNext { view?.showLoading(true) }
                .flatMap { searchInteractor.searchStations(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = {
                    view?.setData(it)
                    view?.selectMedia(mediaInteractor.currentMedia.uri)
                    view?.showLoading(false)
                    view?.showPlaceholder(it.isEmpty())
                }, onError = {
                    view?.showLoading(false)
                    view?.showPlaceholder(true)
                })
    }

    fun changeQuery(newText: String) {
        searchSub?.dispose()
        suggestionSub?.dispose()

        searchInteractor.queryRecentSuggestions(newText)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onSuccess = { view?.addRecentSuggestions(it) })
                .addTo(viewSubs)

        if (newText.isBlank()) return

        suggestionSub = searchInteractor.queryRegularSuggestions(newText)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view?.addRegularSuggestions(it) })
    }

    fun deleteRecentSuggestion(suggestion: Suggestion, curQuery: String) {
        searchInteractor.deleteRecentSuggestion(suggestion)
                .andThen(searchInteractor.queryRecentSuggestions(curQuery))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onSuccess = { view?.addRecentSuggestions(it) })
                .addTo(viewSubs)
    }
}
