package io.github.vladimirmi.internetradioplayer.presentation.stationlist

import android.support.v4.media.session.PlaybackStateCompat
import android.view.MenuItem
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.domain.model.FlatStationsList
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.root.MenuItemHolder
import io.github.vladimirmi.internetradioplayer.presentation.root.RootPresenter
import io.github.vladimirmi.internetradioplayer.presentation.root.ToolbarBuilder
import io.github.vladimirmi.internetradioplayer.ui.base.BasePresenter
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
@Inject constructor(private val rootPresenter: RootPresenter,
                    private val interactor: StationInteractor,
                    private val controlsInteractor: PlayerControlsInteractor,
                    private val router: Router)
    : BasePresenter<StationListView>() {

    private val addStationItem = MenuItemHolder(R.string.menu_add_station, R.drawable.ic_add, order = 0)
    private val exitItem = MenuItemHolder(R.string.menu_exit, R.drawable.ic_exit, order = 1)

    private val actions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.string.menu_add_station -> viewState.openAddStationDialog()
            R.string.menu_exit -> exit()
        }
    }

    private val builder = ToolbarBuilder().setToolbarTitleId(R.string.app_name)
            .setMenuActions(actions)
            .addMenuItem(addStationItem)
            .addMenuItem(exitItem)


    override fun onFirstViewAttach() {
        rootPresenter.viewState.showControls(true)
        interactor.stationsListObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy { viewState.setStations(it) }
                .addTo(compDisp)

        interactor.currentStationObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    viewState.buildToolbar(builder.setToolbarTitle(it.name))
                    viewState.selectStation(it)
                }.addTo(compDisp)

        controlsInteractor.playbackStateObs
                .subscribe { viewState.setPlaying(it.state == PlaybackStateCompat.STATE_PLAYING) }
                .addTo(compDisp)
    }

    fun selectStation(station: Station) {
        interactor.currentStation = station
    }

    fun selectGroup(id: String) {
        interactor.expandOrCollapseGroup(id)
                .subscribeOn(Schedulers.io())
                .subscribe()
                .addTo(compDisp)
    }

    fun removeStation() {
        interactor.removeStation(interactor.currentStation.id)
                .subscribeOn(Schedulers.io())
                .subscribe()
                .addTo(compDisp)
    }

    fun showStation(station: Station) {
        selectStation(station)
        router.showStationSlide(station.id)
    }

    private fun exit() {
        controlsInteractor.stop()
        router.exit()
    }

    fun moveGroupElements(stations: FlatStationsList) {
        interactor.moveGroupElements(stations)
                .subscribeOn(Schedulers.io())
                .subscribe()
                .addTo(compDisp)
    }
}


