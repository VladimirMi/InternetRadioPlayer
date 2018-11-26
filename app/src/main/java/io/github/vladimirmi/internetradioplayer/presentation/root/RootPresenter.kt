package io.github.vladimirmi.internetradioplayer.presentation.root

import android.annotation.SuppressLint
import android.net.Uri
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.interactor.FavoriteListInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.MainInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerInteractor
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
                    private val playerInteractor: PlayerInteractor,
                    private val stationInteractor: StationInteractor,
                    private val favoriteListInteractor: FavoriteListInteractor,
                    private val mainInteractor: MainInteractor)
    : BasePresenter<RootView>() {


    override fun onFirstAttach(view: RootView) {
        playerInteractor.connect()
        val pageId = mainInteractor.getMainPageId()
        router.newRootScreen(pageId)

        favoriteListInteractor.initFavoriteList()
                .ioToMain()
                .subscribeX(onComplete = { view.checkIntent() })
                .addTo(dataSubs)
    }

    override fun onAttach(view: RootView) {
        if (!isFirstAttach) view.checkIntent()
    }

    override fun onDestroy() {
        playerInteractor.disconnect()
    }

    @SuppressLint("CheckResult")
    fun addStation(uri: Uri, startPlay: Boolean) {
//        val station = stationInteractor.getStation { it.uri == uri.toString() }
//        if (station != null) {
//            stationInteractor.currentStation = station
////            router.showStationReplace(station.id)
//            Timber.e("addStation: existed ${station.name}")
//            if (startPlay) playerInteractor.play()
//            return
//        }

        stationInteractor.createStation(uri)
                .ioToMain()
                .doOnSubscribe { view?.showLoadingIndicator(true) }
                .doFinally { view?.showLoadingIndicator(false) }
                .subscribeX(onSuccess = {
                    Timber.e("addStation: ${it.name}")
//                    router.showStationSlide(stationInteractor.station.id)
                }).addTo(viewSubs)
    }

    @SuppressLint("CheckResult")
    fun showStation(id: String, startPlay: Boolean) {
//        val station = stationInteractor.getStation { it.id == id }
//        if (station != null) {
//            stationInteractor.currentStation = station
//            Timber.e("showStation: ${station.name}")
////            router.showStationReplace(station.id)
//            if (startPlay) playerInteractor.play()
//        } else {
//            view?.showMessage(R.string.msg_shortcut_remove)
//        }
    }

    fun navigateTo(navId: Int) {
        when (navId) {
            R.id.nav_exit -> exitApp()
            R.id.nav_settings -> router.navigateTo(navId)
            else -> router.replaceScreen(navId)
        }
    }

    fun exitApp() {
        playerInteractor.stop()
        router.finishChain()
    }

    fun openSettings() {
        Timber.e("openSettings: ")
    }
}
