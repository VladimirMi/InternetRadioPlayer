package io.github.vladimirmi.internetradioplayer.presentation.root

import android.annotation.SuppressLint
import android.net.Uri
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.ioToMain
import io.github.vladimirmi.internetradioplayer.extensions.subscribeByEx
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenterLegacy
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

@InjectViewState
class RootPresenter
@Inject constructor(private val router: Router,
                    private val controlsInteractor: PlayerControlsInteractor,
                    private val stationInteractor: StationInteractor)
    : BasePresenterLegacy<RootView>() {

    private var firstAttach = true

    override fun onFirstViewAttach() {
        controlsInteractor.connect()

        stationInteractor.initStations()
                .ioToMain()
                .subscribeByEx(onComplete = {
                    setupRootScreen()
                    viewState.checkIntent()
                })
                .addTo(subs)

        stationInteractor.currentStationObs
                .map { !it.isNull() }
                .distinctUntilChanged()
                .ioToMain()
                .subscribe(viewState::showControls)
                .addTo(subs)
        firstAttach = false
    }

    override fun attachView(view: RootView?) {
        if (!firstAttach) viewState.checkIntent()
        super.attachView(view)
    }

    override fun onDestroy() {
        controlsInteractor.disconnect()
    }

    @SuppressLint("CheckResult")
    fun addStation(uri: Uri, startPlay: Boolean) {
        val station = stationInteractor.getStation { it.uri == uri.toString() }
        if (station != null) {
            stationInteractor.currentStation = station
            router.showStationReplace(station.id)
            if (startPlay) controlsInteractor.play()
            return
        }

        stationInteractor.createStation(uri)
                .ioToMain()
                .doOnSubscribe { viewState.showLoadingIndicator(true) }
                .doFinally { viewState.showLoadingIndicator(false) }
                .subscribeByEx(onSuccess = {
                    router.showStationSlide(stationInteractor.currentStation.id)
                }).addTo(subs)
    }

    @SuppressLint("CheckResult")
    fun showStation(id: String, startPlay: Boolean) {
        val station = stationInteractor.getStation { it.id == id }
        if (station != null) {
            stationInteractor.currentStation = station
            router.showStationReplace(station.id)
            if (startPlay) controlsInteractor.play()
        } else {
            viewState.showToast(R.string.toast_shortcut_remove)
        }
    }

    fun openSettings() {
        router.navigateTo(Router.SETTINGS_SCREEN)
    }

    fun exitApp() {
        controlsInteractor.stop()
        router.finishChain()
    }

    private fun setupRootScreen() {
        if (stationInteractor.haveStations()) {
            router.newRootScreen(Router.STATIONS_LIST_SCREEN)
        } else {
            router.newRootScreen(Router.GET_STARTED_SCREEN)
        }
    }
}
