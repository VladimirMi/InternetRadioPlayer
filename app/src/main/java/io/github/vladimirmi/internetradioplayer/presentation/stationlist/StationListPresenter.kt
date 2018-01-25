package io.github.vladimirmi.internetradioplayer.presentation.stationlist

import android.view.MenuItem
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.model.entity.Filter
import io.github.vladimirmi.internetradioplayer.model.entity.Station
import io.github.vladimirmi.internetradioplayer.model.entity.groupedlist.GroupedList
import io.github.vladimirmi.internetradioplayer.model.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.model.repository.MediaController
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.root.MenuItemHolder
import io.github.vladimirmi.internetradioplayer.presentation.root.RootPresenter
import io.github.vladimirmi.internetradioplayer.presentation.root.ToolbarBuilder
import io.github.vladimirmi.internetradioplayer.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

@InjectViewState
class StationListPresenter
@Inject constructor(private val rootPresenter: RootPresenter,
                    private val interactor: StationInteractor,
                    private val mediaController: MediaController,
                    private val router: Router)
    : BasePresenter<StationListView>() {

    private val addStationItem = MenuItemHolder(R.string.menu_add_station, R.drawable.ic_add)
    private val favoriteOnItem = MenuItemHolder(R.string.menu_favorite_on, R.drawable.ic_star_empty)
    private val favoriteOffItem = MenuItemHolder(R.string.menu_favorite_off, R.drawable.ic_star)

    private val actions: (MenuItem) -> Unit = {
        when (it.itemId) {
            R.string.menu_favorite_on -> interactor.filterStations(Filter.FAVORITE)
            R.string.menu_favorite_off -> interactor.filterStations(Filter.DEFAULT)
            R.string.menu_add_station -> viewState.openAddStationDialog()
        }
    }

    private val builder = ToolbarBuilder().setToolbarTitleId(R.string.app_name)
            .setMenuActions(actions)
            .addMenuItem(addStationItem)


    override fun onFirstViewAttach() {
        rootPresenter.viewState.showControls(true)
        interactor.stationListObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy { handleStationList(it) }
                .addTo(compDisp)

        interactor.currentStationObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    viewState.buildToolbar(builder.setToolbarTitle(it.name))
                    viewState.selectItem(it, mediaController.isPlaying)
                }.addTo(compDisp)

        mediaController.playbackState
                .subscribe { viewState.selectItem(interactor.currentStation, mediaController.isPlaying) }
                .addTo(compDisp)
    }

    private fun handleStationList(it: GroupedList<Station>) {
        val newBuilder = when (it.filter) {
            Filter.FAVORITE -> {
                if (it.canFilter(Filter.DEFAULT)) {
                    builder.replaceMenuItem(R.string.menu_favorite_on, favoriteOffItem, add = true)
                } else {
                    builder.removeMenuItem(R.string.menu_favorite_off)
                }
            }
            Filter.DEFAULT -> {
                if (it.size == 0) {
                    router.newRootScreen(Router.GET_STARTED_SCREEN)
                    return
                }

                if (it.canFilter(Filter.FAVORITE)) {
                    builder.replaceMenuItem(R.string.menu_favorite_off, favoriteOnItem, add = true)
                } else {
                    builder.removeMenuItem(R.string.menu_favorite_on)
                }
            }
        }
        viewState.buildToolbar(newBuilder)
        viewState.setMediaList(it)
    }

    fun select(station: Station) {
        interactor.currentStation = station
    }

    fun selectGroup(group: String) {
        interactor.showOrHideGroup(group)
    }

    fun removeStation(station: Station) {
        interactor.removeStation(station)
                .subscribe { interactor.removeShortcut(station) }
                .addTo(compDisp)
    }

    fun showStation(station: Station) {
        select(station)
        router.showStationSlide(station)
    }
}


