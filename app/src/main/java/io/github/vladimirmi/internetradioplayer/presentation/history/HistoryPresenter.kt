package io.github.vladimirmi.internetradioplayer.presentation.history

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.interactor.FavoriteListInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.HistoryInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.MediaInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 02.12.2018.
 */

class HistoryPresenter
@Inject constructor(private val historyInteractor: HistoryInteractor,
                    private val favoriteListInteractor: FavoriteListInteractor,
                    private val stationInteractor: StationInteractor,
                    private val mediaInteractor: MediaInteractor)
    : BasePresenter<HistoryView>() {

    override fun onAttach(view: HistoryView) {
        Observables.combineLatest(historyInteractor.getHistoryObs(),
                favoriteListInteractor.stationsListObs) { history, _ ->
            //todo isFavorite field in Station
            history.map { station -> station to favoriteListInteractor.isFavorite(station) }
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = {
                    view.setHistory(it)
                    view.showPlaceholder(it.isEmpty())
                })
                .addTo(viewSubs)

        mediaInteractor.currentMediaObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.selectStation(it.uri) })
                .addTo(viewSubs)
    }

    fun selectStation(station: Station) {
        mediaInteractor.currentMedia = station
    }

    fun switchFavorite() {
        val station = mediaInteractor.currentMedia as? Station ?: return
        stationInteractor.switchFavorite(station)
                .subscribeX()
                .addTo(dataSubs)
    }

    fun deleteHistory(station: Station) {
        historyInteractor.deleteHistory(station)
                .subscribeX()
                .addTo(dataSubs)
    }
}