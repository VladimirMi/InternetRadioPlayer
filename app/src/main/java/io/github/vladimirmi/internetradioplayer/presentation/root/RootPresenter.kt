package io.github.vladimirmi.internetradioplayer.presentation.root

import android.annotation.SuppressLint
import android.net.Uri
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.interactor.*
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

class RootPresenter
@Inject constructor(private val router: Router,
                    private val playerInteractor: PlayerInteractor,
                    private val stationInteractor: StationInteractor,
                    private val favoriteListInteractor: FavoriteListInteractor,
                    private val mainInteractor: MainInteractor,
                    private val historyInteractor: HistoryInteractor)
    : BasePresenter<RootView>() {

    override fun onFirstAttach(view: RootView) {
        playerInteractor.connect()
        val pageId = mainInteractor.getMainPageId()
        router.newRootScreen(pageId)


        favoriteListInteractor.initFavoriteList()
                .andThen(historyInteractor.selectRecentStation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX { view.checkIntent() }
                .addTo(dataSubs)
    }

    override fun onAttach(view: RootView) {
        if (!isFirstAttach) view.checkIntent()
    }

    override fun onDestroy() {
        playerInteractor.disconnect()
    }

    @SuppressLint("CheckResult")
    fun addOrShowStation(uri: Uri, startPlay: Boolean) {

        stationInteractor.createStation(uri)
                .doOnSuccess { if (startPlay) playerInteractor.play() }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view?.showLoadingIndicator(true) }
                .doFinally { view?.showLoadingIndicator(false) }
                .subscribeX(onSuccess = {
                    navigateTo(R.id.nav_player)
                }).addTo(viewSubs)
    }

    @SuppressLint("CheckResult")
    fun showStation(id: String, startPlay: Boolean) {
        //todo legacy
        val station = favoriteListInteractor.getStation(id)
        if (station != null) {
            stationInteractor.station = station
            navigateTo(R.id.nav_player)
            if (startPlay) playerInteractor.play()
        } else {
            view?.showMessage(R.string.msg_shortcut_remove)
        }
    }

    fun navigateTo(navId: Int) {
        when (navId) {
            R.id.nav_exit -> exitApp()
            R.id.nav_settings -> router.navigateTo(navId)
            else -> router.replaceScreen(navId)
        }
    }

    private fun exitApp() {
        playerInteractor.stop()
        router.finishChain()
    }
}
