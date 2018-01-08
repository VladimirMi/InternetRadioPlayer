package io.github.vladimirmi.radius.presentation.stationlist

import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.interactor.StationInteractor
import io.github.vladimirmi.radius.model.repository.MediaController
import io.github.vladimirmi.radius.navigation.Router
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

    val builder get() = ToolbarBuilder()

    override fun onFirstViewAttach() {
        rootPresenter.viewState.showControls(true)
        builder.setToolbarTitleId(R.string.app_name)
        viewState.buildToolbar(builder)

        interactor.stationListObs()
                .subscribeBy { viewState.setMediaList(it) }
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
//        viewState.notifyList()
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


