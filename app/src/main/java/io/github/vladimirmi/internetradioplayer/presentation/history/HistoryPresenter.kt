package io.github.vladimirmi.internetradioplayer.presentation.history

import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.domain.interactor.HistoryInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.MediaInteractor
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 02.12.2018.
 */

class HistoryPresenter
@Inject constructor(private val historyInteractor: HistoryInteractor,
                    private val mediaInteractor: MediaInteractor)
    : BasePresenter<HistoryView>() {

    override fun onAttach(view: HistoryView) {
        historyInteractor.getHistoryObs()
                .observeOn(AndroidSchedulers.mainThread())
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

    fun deleteHistory(station: Station) {
        historyInteractor.deleteHistory(station)
                .subscribeX()
                .addTo(dataSubs)
    }
}