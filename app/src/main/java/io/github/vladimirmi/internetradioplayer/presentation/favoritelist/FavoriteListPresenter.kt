package io.github.vladimirmi.internetradioplayer.presentation.favoritelist

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.interactor.FavoriteListInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class FavoriteListPresenter
@Inject constructor(private val stationInteractor: StationInteractor,
                    private val favoriteListInteractor: FavoriteListInteractor,
                    private val router: Router)
    : BasePresenter<StationListView>() {

    override fun onAttach(view: StationListView) {
        favoriteListInteractor.stationsListObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.setStations(it) })
                .addTo(viewSubs)

        stationInteractor.stationObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.selectStation(it) })
                .addTo(viewSubs)
    }

    fun selectStation(station: Station) {
        stationInteractor.station = station
    }

    fun selectGroup(id: String) {
//        stationInteractor.expandOrCollapseGroup(id)
//                .subscribeOn(Schedulers.io())
//                .subscribeX()
//                .addTo(viewSubs)
    }

    fun removeStation() {
//        stationInteractor.removeStation(stationInteractor.currentStation.id)
//                .subscribeOn(Schedulers.io())
//                .subscribeX()
//                .addTo(viewSubs)
    }

    fun showStation(station: Station) {
        Timber.e("showStation: ${station.name}")
//        selectStation(station)
//        router.showStationSlide(station.id)
    }

    fun moveGroupElements(stations: FlatStationsList) {
//        stationInteractor.moveGroupElements(stations)
//                .subscribeOn(Schedulers.io())
//                .subscribeX()
//                .addTo(viewSubs)
    }
}


