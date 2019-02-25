package io.github.vladimirmi.internetradioplayer.presentation.favoritelist.stations

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.interactor.FavoriteListInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.MediaInteractor
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

class FavoriteStationsPresenter
@Inject constructor(private val favoriteListInteractor: FavoriteListInteractor,
                    private val mediaInteractor: MediaInteractor)
    : BasePresenter<FavoriteStationsView>() {

    override fun onAttach(view: FavoriteStationsView) {
        favoriteListInteractor.stationsListObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = {
                    view.setStations(it)
                    view.showPlaceholder(it.size == 0)
                })
                .addTo(viewSubs)

        mediaInteractor.currentStationObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.selectStation(it) })
                .addTo(viewSubs)
    }

    fun selectStation(station: Station) {
        mediaInteractor.currentMedia = station
    }

    fun selectGroup(id: String) {
        favoriteListInteractor.expandOrCollapseGroup(id)
                .subscribeX()
                .addTo(dataSubs)
    }

    fun removeGroup(id: String) {
        favoriteListInteractor.removeGroup(id)
                .subscribeX()
                .addTo(dataSubs)
    }

    fun moveGroupElements(stations: FlatStationsList) {
        favoriteListInteractor.moveGroupElements(stations)
                .subscribeX()
                .addTo(dataSubs)
    }
}