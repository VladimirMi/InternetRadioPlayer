package io.github.vladimirmi.internetradioplayer.presentation.data

import io.github.vladimirmi.internetradioplayer.data.net.UberStationsService
import io.github.vladimirmi.internetradioplayer.domain.interactor.MediaInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.SearchInteractor
import io.github.vladimirmi.internetradioplayer.domain.model.Data
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

class DataPresenter
@Inject constructor(private val searchInteractor: SearchInteractor,
                    private val mediaInteractor: MediaInteractor) : BasePresenter<DataView>() {

    private var selectSub: Disposable? = null

    override fun onAttach(view: DataView) {
        mediaInteractor.currentMediaObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.selectData(it.uri) })
                .addTo(viewSubs)
    }


    fun fetchData(endpoint: String?, query: String?) {
        if (endpoint == null || query == null) return

        //todo to interactor
        val fetchData = when (endpoint) {
            UberStationsService.STATIONS_ENDPOINT -> searchInteractor.searchStations(query)
            UberStationsService.TOPSONGS_ENDPOINT -> searchInteractor.searchTopSongs(query)
            UberStationsService.TALKS_ENDPOINT -> searchInteractor.searchTalks(query)
            else -> return
        }

        fetchData.observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view?.showLoading(true) }
                .subscribeX(onSuccess = {
                    view?.setData(it)
                    view?.selectData(mediaInteractor.currentMedia.uri)
                    view?.showLoading(false)
                }, onError = {
                    view?.showLoading(false)
                })

    }

    fun selectData(data: Data) {
        selectSub?.dispose()
        selectSub = searchInteractor.selectUberStation(data.stationId)
                .subscribeX()
    }
}