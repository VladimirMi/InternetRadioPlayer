package io.github.vladimirmi.internetradioplayer.presentation.search

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.domain.interactor.FavoriteListInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.MediaInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.SearchInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
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

class SearchPresenter
@Inject constructor(private val searchInteractor: SearchInteractor,
                    private val stationInteractor: StationInteractor,
                    private val mediaInteractor: MediaInteractor,
                    private val favoriteListInteractor: FavoriteListInteractor)
    : BasePresenter<SearchView>() {

    var intervalSearchEnabled: Boolean = false
    private var searchSub: Disposable? = null
    private var suggestionSub: Disposable? = null
    private var selectSub: Disposable? = null

    override fun onFirstAttach(view: SearchView) {
        view.showPlaceholder(true)
        searchInteractor.queryRecentSuggestions("")
                .filter { it.isNotEmpty() }
                .map { it.first() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.onSuggestionSelected(it) })
                .addTo(viewSubs)
    }

    override fun onAttach(view: SearchView) {
        //todo refactor like in history
        mediaInteractor.currentMediaObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.selectStation(it.uri) })
                .addTo(viewSubs)

        favoriteListInteractor.stationsListObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.setFavorites(it) })
                .addTo(viewSubs)
    }

    override fun onDetach() {
        searchSub?.dispose()
        suggestionSub?.dispose()
    }

    fun selectStation(station: StationSearchRes) {
        selectSub?.dispose()
        selectSub = searchInteractor.selectUberStation(station.id)
                .subscribeX()
    }

    fun switchFavorite() {
        val station = mediaInteractor.currentMedia as? Station ?: return
        stationInteractor.switchFavorite(station)
                .subscribeX()
                .addTo(dataSubs)
    }

    fun submitSearch(query: String) {
        searchSub?.dispose()

        searchSub = Observable.interval(0, 60, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .map { query.trim() }
                .filter { intervalSearchEnabled && it.isNotEmpty() }
                .doOnNext { if (it.length < 3) view?.showToast(R.string.msg_text_short) }
                .filter { it.length > 2 }
                .doOnNext { view?.showLoading(true) }
                .flatMapSingle { searchInteractor.searchStations(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = {
                    view?.setStations(it)
                    view?.selectStation(mediaInteractor.currentMedia.id)
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
