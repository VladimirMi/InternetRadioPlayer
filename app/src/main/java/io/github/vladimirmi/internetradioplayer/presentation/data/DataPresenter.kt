package io.github.vladimirmi.internetradioplayer.presentation.data

import io.github.vladimirmi.internetradioplayer.domain.interactor.MediaInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.SearchInteractor
import io.github.vladimirmi.internetradioplayer.domain.model.Media
import io.github.vladimirmi.internetradioplayer.domain.model.SearchState
import io.github.vladimirmi.internetradioplayer.extensions.errorHandler
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.github.vladimirmi.internetradioplayer.utils.MessageResException
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
                .subscribeX(onNext = { view.selectMedia(it) })
                .addTo(viewSubs)
    }

    fun fetchData(endpoint: String?, query: String?) {
        searchInteractor.search(endpoint, query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { handleSearch(it) })

    }

    fun selectMedia(media: Media) {
        selectSub?.dispose()
        selectSub = searchInteractor
                .selectMedia(media)
                .subscribeX()
    }

    private fun handleSearch(state: SearchState) {
        when (state) {
            is SearchState.Loading -> {
                view?.showLoading(true)
            }
            is SearchState.Data -> {
                val data = state.data
                view?.setData(data)
                view?.selectMedia(mediaInteractor.currentMedia)
                view?.showLoading(false)
            }
            is SearchState.Error -> {
                val error = state.error
                if (error is MessageResException) view?.showToast(error.resId)
                else errorHandler.invoke(error)
                view?.showLoading(false)
            }
        }
    }
}