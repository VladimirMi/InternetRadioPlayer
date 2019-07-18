package io.github.vladimirmi.internetradioplayer.presentation.root

import android.annotation.SuppressLint
import android.net.Uri
import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.interactor.*
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.github.vladimirmi.internetradioplayer.presentation.navigation.Router
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
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

    private var playerVisibilitySub: Disposable? = null

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

    fun checkPlayerVisibility(isPlayerEnabled: Boolean) {
        playerVisibilitySub?.dispose(); playerVisibilitySub = null
        playerVisibilitySub = mediaInteractor.currentMediaObs
                .map { !it.isNull() }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = {
                    if (it && isPlayerEnabled) view?.collapsePlayer()
                    else view?.hidePlayer()
                })
    }

    @SuppressLint("CheckResult")
    fun createStation(uri: Uri, name: String?, addToFavorite: Boolean, startPlay: Boolean) {
        stationInteractor.createStation(uri, name)
                .flatMapCompletable {
                    if (addToFavorite) stationInteractor.addToFavorite(it)
                    else mediaInteractor.setMedia(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view?.showLoadingIndicator(true) }
                .doFinally { view?.showLoadingIndicator(false) }
                .subscribeX(onComplete = {
                    if (addToFavorite) router.replaceScreen(R.id.nav_favorites)
                    if (startPlay) playerInteractor.play()
                }).addTo(viewSubs)
    }

    @SuppressLint("CheckResult")
    fun showStation(id: String, startPlay: Boolean) {
        //todo legacy
        Timber.e("showStation: $id $startPlay")
        val station = favoriteListInteractor.getStation(id)
        if (station != null) {
            mediaInteractor.currentMedia = station
            router.replaceScreen(R.id.nav_favorites)
            if (startPlay) playerInteractor.play()
        } else {
            view?.showSnackbar(R.string.msg_shortcut_remove)
        }
    }
}
