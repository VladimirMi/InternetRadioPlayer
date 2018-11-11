package io.github.vladimirmi.internetradioplayer.presentation.stationlist

import android.support.v4.media.session.PlaybackStateCompat
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.extensions.subscribeByEx
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.github.vladimirmi.internetradioplayer.presentation.base.MenuItemHolder
import io.github.vladimirmi.internetradioplayer.presentation.base.ToolbarBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class StationListPresenter
@Inject constructor(private val interactor: StationInteractor,
                    private val controlsInteractor: PlayerControlsInteractor,
                    private val router: Router)
    : BasePresenter<StationListView>() {

    private val builder = ToolbarBuilder.standard()
            .addMenuItem(MenuItemHolder(R.string.menu_add_station, R.drawable.ic_add, order = 0))
            .setMenuActions {
                if (it.itemId == R.string.menu_add_station) view?.openAddStationDialog()
            }

    override fun onAttach(view: StationListView) {
        interactor.stationsListObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy { view.setStations(it) }
                .addTo(viewSubs)

        interactor.currentStationObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    view.buildToolbar(builder.setToolbarTitle(it.name))
                    view.selectStation(it)
                }.addTo(viewSubs)

        controlsInteractor.playbackStateObs
                .subscribe { view.setPlaying(it.state == PlaybackStateCompat.STATE_PLAYING) }
                .addTo(viewSubs)
    }

    fun selectStation(station: Station) {
        interactor.currentStation = station
    }

    fun selectGroup(id: String) {
        interactor.expandOrCollapseGroup(id)
                .subscribeOn(Schedulers.io())
                .subscribeByEx()
                .addTo(viewSubs)
    }

    fun removeStation() {
        interactor.removeStation(interactor.currentStation.id)
                .subscribeOn(Schedulers.io())
                .subscribeByEx()
                .addTo(viewSubs)
    }

    fun showStation(station: Station) {
        Timber.e("showStation: ${station.name}")
//        selectStation(station)
//        router.showStationSlide(station.id)
    }

    fun moveGroupElements(stations: FlatStationsList) {
        interactor.moveGroupElements(stations)
                .subscribeOn(Schedulers.io())
                .subscribeByEx()
                .addTo(viewSubs)
    }
}


