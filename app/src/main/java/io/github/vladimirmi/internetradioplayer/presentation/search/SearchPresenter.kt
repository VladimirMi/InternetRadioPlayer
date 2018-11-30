package io.github.vladimirmi.internetradioplayer.presentation.search

import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.domain.interactor.FavoriteListInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.SearchInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
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

    override fun onAttach(view: SearchView) {
        stationInteractor.stationObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.selectStation(it) })
                .addTo(viewSubs)

        favoriteListInteractor.stationsListObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.setFavorites(it) })
                .addTo(viewSubs)
    }

    fun setSearchViewObservable(observable: Observable<SearchEvent>) {

        observable.filter { it is SearchEvent.Change }
                .flatMapSingle { searchInteractor.queryRecentSuggestions(it.query) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view?.addRecentSuggestions(it) })
                .addTo(viewSubs)

        observable.filter { it is SearchEvent.Change && it.query.isNotEmpty() }
                .debounce(600, TimeUnit.MILLISECONDS)
                .switchMapSingle { searchInteractor.queryRegularSuggestions(it.query) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view?.addRegularSuggestions(it) })
                .addTo(viewSubs)

        observable.filter { it is SearchEvent.Submit }
                .flatMapSingle { searchInteractor.searchStations(it.query) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view?.setStations(it) })
                .addTo(viewSubs)

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
}
