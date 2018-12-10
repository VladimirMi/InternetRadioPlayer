package io.github.vladimirmi.internetradioplayer.presentation.history

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.interactor.FavoriteListInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.HistoryInteractor
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
                    private val stationInteractor: StationInteractor)
    : BasePresenter<HistoryView>() {

    override fun onAttach(view: HistoryView) {
        Observables.combineLatest(historyInteractor.getHistoryObs(),
                favoriteListInteractor.stationsListObs) { history, favorites ->
            //todo isFavorite field in Station
            history.map { station -> station to (favorites.findStation { it.id == station.id } != null) }
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.setHistory(it) })
                .addTo(viewSubs)

        stationInteractor.stationObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.selectStation(it) })
                .addTo(viewSubs)
    }

    fun selectStation(station: Station) {
        stationInteractor.station = station
    }

    fun switchFavorite(stationFavorite: Pair<Station, Boolean>) {
        val changeFavorite = if (stationFavorite.second) {
            stationInteractor.removeFromFavorite()
        } else {
            stationInteractor.addToFavorite()
        }
        changeFavorite.subscribeX()
                .addTo(dataSubs)
    }
}