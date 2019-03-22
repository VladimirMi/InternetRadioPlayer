package io.github.vladimirmi.internetradioplayer.presentation.navigation

import io.github.vladimirmi.internetradioplayer.data.net.UberStationsService
import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.domain.interactor.MediaInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.SearchInteractor
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
        if (endpoint == UberStationsService.STATIONS_ENDPOINT) {
            searchInteractor.searchStations(query!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { view?.showLoading(true) }
                    .subscribeX(onSuccess = {
                        view?.setData(it)
                        view?.selectData(mediaInteractor.currentMedia.uri)
                        view?.showLoading(false)
                    }, onError = {
                        view?.showLoading(false)
                    })
        }
    }

    fun selectData(station: StationSearchRes) {
        selectSub?.dispose()
        selectSub = searchInteractor.selectUberStation(station.id)
                .subscribeX()
    }
}