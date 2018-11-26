package io.github.vladimirmi.internetradioplayer.presentation.stationlist

import android.support.v4.media.session.PlaybackStateCompat
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationListPresenter
@Inject constructor(private val interactor: StationInteractor,
                    private val playerInteractor: PlayerInteractor,
                    private val router: Router)
    : BasePresenter<StationListView>() {

    override fun onAttach(view: StationListView) {
//        interactor.stationsListObs
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeBy { view.setStations(it) }
//                .addTo(viewSubs)

        interactor.stationObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { view.selectStation(it) }
                .addTo(viewSubs)

        playerInteractor.playbackStateObs
                .subscribe { view.setPlaying(it.state == PlaybackStateCompat.STATE_PLAYING) }
                .addTo(viewSubs)
    }

    fun selectStation(station: Station) {
//        interactor.currentStation = station
    }

    fun selectGroup(id: String) {
//        interactor.expandOrCollapseGroup(id)
//                .subscribeOn(Schedulers.io())
//                .subscribeX()
//                .addTo(viewSubs)
    }

    fun removeStation() {
//        interactor.removeStation(interactor.currentStation.id)
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
//        interactor.moveGroupElements(stations)
//                .subscribeOn(Schedulers.io())
//                .subscribeX()
//                .addTo(viewSubs)
    }
}


