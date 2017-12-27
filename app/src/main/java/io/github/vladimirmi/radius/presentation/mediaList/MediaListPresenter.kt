package io.github.vladimirmi.radius.presentation.mediaList

import android.support.v4.media.session.PlaybackStateCompat
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.model.entity.Station
import io.github.vladimirmi.radius.model.interactor.StationInteractor
import io.github.vladimirmi.radius.model.repository.MediaController
import io.github.vladimirmi.radius.navigation.Router
import io.github.vladimirmi.radius.presentation.root.RootPresenter
import io.github.vladimirmi.radius.presentation.root.ToolbarBuilder
import io.github.vladimirmi.radius.ui.base.BasePresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

@InjectViewState
class MediaListPresenter
@Inject constructor(private val rootPresenter: RootPresenter,
                    private val interactor: StationInteractor,
                    private val mediaController: MediaController,
                    private val router: Router)
    : BasePresenter<MediaListView>() {

    val builder get() = ToolbarBuilder()

    override fun onFirstViewAttach() {
        rootPresenter.viewState.showControls(true)
        builder.setToolbarTitleId(R.string.app_name)
        viewState.buildToolbar(builder)

        interactor.stationListObs()
                .subscribeBy { viewState.setMediaList(it) }
                .addTo(compDisp)

        Observable.combineLatest(interactor.currentStationObs(), mediaController.playbackState,
                BiFunction { station: Station, _: PlaybackStateCompat -> station })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    viewState.buildToolbar(builder.setToolbarTitle(it.title))
                    viewState.selectItem(it, mediaController.isPlaying)
                }
                .addTo(compDisp)
    }

    fun select(station: Station) {
        interactor.currentStation = station
    }

    fun selectGroup(group: String) {
        interactor.showOrHideGroup(group)
        viewState.notifyList()
    }

    fun removeStation(station: Station) {
        viewState.openRemoveDialog(station)
    }

    fun submitRemove(station: Station) {
        interactor.removeStation(station)
        viewState.closeRemoveDialog()
    }

    fun cancelRemove() {
        viewState.closeRemoveDialog()
    }

    fun showStation() {
        router.showStation(interactor.currentStation)
    }
}


