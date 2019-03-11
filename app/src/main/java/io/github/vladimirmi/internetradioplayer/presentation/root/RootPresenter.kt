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
                    private val mediaInteractor: MediaInteractor,
                    private val mainInteractor: MainInteractor,
                    private val historyInteractor: HistoryInteractor,
                    private val recordsInteractor: RecordsInteractor)
    : BasePresenter<RootView>() {

    override fun onFirstAttach(view: RootView) {
        router.newRootScreen(mainInteractor.getMainPageId())

        playerInteractor.connect()
                .andThen(favoriteListInteractor.initFavoriteList()
                        .mergeWith(recordsInteractor.initRecords())
                        .mergeWith(historyInteractor.initHistory()))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view.showLoadingIndicator(true) }
                .doOnTerminate { view.showLoadingIndicator(false) }
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
    fun addOrShowStation(uri: Uri, addToFavorite: Boolean, startPlay: Boolean) {
        stationInteractor.createStation(uri)
                .flatMapCompletable {
                    if (addToFavorite) stationInteractor.addToFavorite(it)
                    else mediaInteractor.setMedia(it)
                }
                .doOnComplete { if (startPlay) playerInteractor.play() }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view?.showLoadingIndicator(true) }
                .doFinally { view?.showLoadingIndicator(false) }
                .subscribeX(onComplete = {
                    navigateTo(R.id.nav_favorites)
                }).addTo(viewSubs)
    }

    @SuppressLint("CheckResult")
    fun showStation(id: String, startPlay: Boolean) {
        //todo legacy
        Timber.e("showStation: $id $startPlay")
        val station = favoriteListInteractor.getStation(id)
        if (station != null) {
            mediaInteractor.currentMedia = station
            navigateTo(R.id.nav_favorites)
            if (startPlay) playerInteractor.play()
        } else {
            view?.showSnackbar(R.string.msg_shortcut_remove)
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
        recordsInteractor.stopAllRecordings()
        router.finishChain()
    }
}
