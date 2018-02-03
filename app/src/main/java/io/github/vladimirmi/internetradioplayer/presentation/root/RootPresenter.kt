package io.github.vladimirmi.internetradioplayer.presentation.root

import android.net.Uri
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.extensions.ioToMain
import io.github.vladimirmi.internetradioplayer.model.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.model.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

@InjectViewState
class RootPresenter
@Inject constructor(private val router: Router,
                    private val controlsInteractor: PlayerControlsInteractor,
                    private val stationInteractor: StationInteractor)
    : BasePresenter<RootView>() {

    override fun onFirstViewAttach() {
        controlsInteractor.connect()
        stationInteractor.initStations()
                .ioToMain()
                .subscribe { setupRootScreen() }
                .addTo(compDisp)
    }

    override fun onDestroy() {
        controlsInteractor.disconnect()
    }

    fun addStation(uri: Uri) {
        stationInteractor.createStation(uri)
                .ioToMain()
                .doOnSubscribe { viewState.showLoadingIndicator(true) }
                .doFinally { viewState.showLoadingIndicator(false) }
                .subscribeBy(
                        onSuccess = {
                            viewState.showControls(true)
                            router.showStationSlide(stationInteractor.currentStation)
                        },
                        onError = {
                            Timber.e(it)
                            viewState.showToast(R.string.toast_add_error)
                        }
                ).addTo(compDisp)
    }

    fun showStation(id: String) {
        val station = stationInteractor.getStation(id)
        if (station != null) {
            stationInteractor.currentStation = station
            router.showStationReplace(station)
        } else viewState.showToast(R.string.toast_shortcut_remove)
    }


    private fun setupRootScreen() {
        if (stationInteractor.haveStations()) {
            router.newRootScreen(Router.MEDIA_LIST_SCREEN)
        } else {
            router.newRootScreen(Router.GET_STARTED_SCREEN)
        }
        viewState.checkIntent()
    }
}