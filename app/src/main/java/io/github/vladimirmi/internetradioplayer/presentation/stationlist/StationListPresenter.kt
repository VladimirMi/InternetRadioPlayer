package io.github.vladimirmi.internetradioplayer.presentation.stationlist

import android.support.v4.media.session.PlaybackStateCompat
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.extensions.subscribeByEx
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenterLegacy
import io.github.vladimirmi.internetradioplayer.presentation.base.MenuItemHolder
import io.github.vladimirmi.internetradioplayer.presentation.base.ToolbarBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

@InjectViewState
class StationListPresenter
@Inject constructor(private val interactor: StationInteractor,
                    private val controlsInteractor: PlayerControlsInteractor,
                    private val router: Router)
    : BasePresenterLegacy<StationListView>() {

    private val builder = ToolbarBuilder.standard()
            .addMenuItem(MenuItemHolder(R.string.menu_add_station, R.drawable.ic_add, order = 0))
            .setMenuActions {
                if (it.itemId == R.string.menu_add_station) viewState.openAddStationDialog()
            }

    override fun onFirstViewAttach() {
        interactor.stationsListObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy { viewState.setStations(it) }
                .addTo(subs)

        interactor.currentStationObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    viewState.buildToolbar(builder.setToolbarTitle(it.name))
                    viewState.selectStation(it)
                }.addTo(subs)

        controlsInteractor.playbackStateObs
                .subscribe { viewState.setPlaying(it.state == PlaybackStateCompat.STATE_PLAYING) }
                .addTo(subs)
    }

    fun selectStation(station: Station) {
        interactor.currentStation = station
    }

    fun selectGroup(id: String) {
        interactor.expandOrCollapseGroup(id)
                .subscribeOn(Schedulers.io())
                .subscribeByEx()
                .addTo(subs)
    }

    fun removeStation() {
        interactor.removeStation(interactor.currentStation.id)
                .subscribeOn(Schedulers.io())
                .subscribeByEx()
                .addTo(subs)
    }

    fun showStation(station: Station) {
        selectStation(station)
        router.showStationSlide(station.id)
    }

    fun moveGroupElements(stations: FlatStationsList) {
        interactor.moveGroupElements(stations)
                .subscribeOn(Schedulers.io())
                .subscribeByEx()
                .addTo(subs)
    }
}


