package io.github.vladimirmi.radius.presentation.stationlist

import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.model.entity.Filter
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.interactor.StationInteractor
import io.github.vladimirmi.radius.model.repository.MediaController
import io.github.vladimirmi.radius.navigation.Router
import io.github.vladimirmi.radius.presentation.root.MenuItemHolder
import io.github.vladimirmi.radius.presentation.root.RootPresenter
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder
import io.github.vladimirmi.radius.ui.base.BasePresenter
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

    private val builder = ToolbarBuilder().setToolbarTitleId(R.string.app_name)

    private val favoriteOn: MenuItemHolder by lazy {
        MenuItemHolder(R.string.menu_favorite, R.drawable.ic_empty_star, {
            interactor.filterStations(Filter.FAVORITE)
        })
    }

    private val favoriteOff: MenuItemHolder by lazy {
        MenuItemHolder(R.string.menu_favorite, R.drawable.ic_star, {
            interactor.filterStations(Filter.DEFAULT)
        })
    }


    override fun onFirstViewAttach() {
        rootPresenter.viewState.showControls(true)
        interactor.stationListObs()
                .subscribeBy {
                    when (it.filter) {
                        Filter.FAVORITE -> {
                            if (it.canFilter(Filter.DEFAULT)) {
                                if (it.itemSize == 0) {
                                    interactor.filterStations(Filter.DEFAULT)
                                } else if (!it.contains(interactor.currentStation)) {
                                    interactor.currentStation = it.firstOrNull()
                                }
                                viewState.buildToolbar(builder.clearActions()
                                        .addAction(favoriteOff))
                            }
                        }
                        Filter.DEFAULT -> {
                            if (it.canFilter(Filter.FAVORITE)) {
                                viewState.buildToolbar(builder.clearActions()
                                        .addAction(favoriteOn))
                            } else {
                                viewState.buildToolbar(builder.clearActions())
                            }
                        }
                    }
                    viewState.setMediaList(it)
                }
                .addTo(compDisp)

        interactor.currentStationObs()
                .subscribe {
                    viewState.buildToolbar(builder.setToolbarTitle(it.name))
                    viewState.selectItem(it, mediaController.isPlaying)
                }.addTo(compDisp)

        mediaController.playbackState
                .subscribe { viewState.selectItem(interactor.currentStation, mediaController.isPlaying) }
                .addTo(compDisp)
    }

    fun select(station: Station) {
        interactor.currentStation = station
    }

    fun selectGroup(group: String) {
        interactor.showOrHideGroup(group)
    }

    fun removeStation(station: Station) {
        interactor.removeStation(station)
                .subscribe()
                .addTo(compDisp)
    }

    fun showStation(station: Station) {
        select(station)
        router.showStation(station)
    }
}


