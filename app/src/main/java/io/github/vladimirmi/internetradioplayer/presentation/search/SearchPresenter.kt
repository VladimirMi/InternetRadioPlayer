package io.github.vladimirmi.internetradioplayer.presentation.search

import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.domain.interactor.FavoriteListInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.SearchInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.Observable
import io.reactivex.Single
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
                    private val favoriteListInteractor: FavoriteListInteractor)
    : BasePresenter<SearchView>() {

    private var searchSub: Disposable? = null
    private var suggestionSub: Disposable? = null

    override fun onFirstAttach(view: SearchView) {
        searchInteractor.queryRecentSuggestions("")
                .filter { it.isNotEmpty() }
                .map { it.last() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.onSuggestionSelected(it) })
                .addTo(viewSubs)
    }

    override fun onAttach(view: SearchView) {
        //todo refactor like in history
        stationInteractor.stationObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.selectStation(it) })
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
        searchInteractor.selectUberStation(station.id)
                .subscribeX()
                .addTo(dataSubs)
    }

    fun switchFavorite() {
        val isFavorite = favoriteListInteractor.isFavorite(stationInteractor.station)
        val changeFavorite = if (isFavorite) stationInteractor.removeFromFavorite()
        else stationInteractor.addToFavorite()
        changeFavorite.subscribeX()
                .addTo(dataSubs)
    }


    fun submitSearch(query: String) {
        searchSub?.dispose()
        searchSub = Observable.interval(0, 60, TimeUnit.SECONDS)
                .flatMapSingle { searchInteractor.searchStations(query) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view?.setStations(it) })

    }

    fun changeQuery(newText: String) {
        searchInteractor.queryRecentSuggestions(newText)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onSuccess = { view?.addRecentSuggestions(it) })
                .addTo(viewSubs)

        suggestionSub?.dispose()
        suggestionSub = Single.just(newText)
                .delaySubscription(600, TimeUnit.MILLISECONDS)
                .flatMap(searchInteractor::queryRecentSuggestions)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onSuccess = { view?.addRecentSuggestions(it) })
    }
}
