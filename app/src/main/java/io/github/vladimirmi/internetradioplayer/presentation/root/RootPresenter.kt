package io.github.vladimirmi.internetradioplayer.presentation.root

import android.annotation.SuppressLint
import android.net.Uri
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.ioToMain
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

class RootPresenter
@Inject constructor(private val router: Router,
                    private val controlsInteractor: PlayerControlsInteractor,
                    private val stationInteractor: StationInteractor)
    : BasePresenter<RootView>() {


    override fun onFirstAttach(view: RootView) {
        controlsInteractor.connect()
        setupRootScreen()

        stationInteractor.initStations()
                .ioToMain()
                .subscribeX(onComplete = { view.checkIntent() })
                .addTo(dataSubs)
    }

    override fun onAttach(view: RootView) {
        if (!isFirstAttach) view.checkIntent()
    }

    override fun onDestroy() {
        controlsInteractor.disconnect()
    }

    @SuppressLint("CheckResult")
    fun addStation(uri: Uri, startPlay: Boolean) {
        val station = stationInteractor.getStation { it.uri == uri.toString() }
        if (station != null) {
            stationInteractor.currentStation = station
//            router.showStationReplace(station.id)
            Timber.e("addStation: existed ${station.name}")
            if (startPlay) controlsInteractor.play()
            return
        }

        stationInteractor.createStation(uri)
                .ioToMain()
                .doOnSubscribe { view?.showLoadingIndicator(true) }
                .doFinally { view?.showLoadingIndicator(false) }
                .subscribeX(onSuccess = {
                    Timber.e("addStation: ${it.name}")
//                    router.showStationSlide(stationInteractor.currentStation.id)
                }).addTo(viewSubs)
    }

    @SuppressLint("CheckResult")
    fun showStation(id: String, startPlay: Boolean) {
        val station = stationInteractor.getStation { it.id == id }
        if (station != null) {
            stationInteractor.currentStation = station
            Timber.e("showStation: ${station.name}")
//            router.showStationReplace(station.id)
            if (startPlay) controlsInteractor.play()
        } else {
            view?.showMessage(R.string.msg_shortcut_remove)
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
        Timber.e("setupRootScreen: ")
        router.newRootScreen(Router.MAIN_SCREEN)
//        if (stationInteractor.haveStations()) {
//            router.newRootScreen(Router.STATIONS_LIST_SCREEN)
//        } else {
//            router.newRootScreen(Router.GET_STARTED_SCREEN)
//        }
    }
}
