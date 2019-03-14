package io.github.vladimirmi.internetradioplayer.presentation.favoritelist

import io.github.vladimirmi.internetradioplayer.domain.interactor.MainInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.RecordsInteractor
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class FavoriteListPresenter
@Inject constructor(private val recordsInteractor: RecordsInteractor,
                    private val mainInteractor: MainInteractor)
    : BasePresenter<FavoriteListView>() {

    override fun onFirstAttach(view: FavoriteListView) {
        view.showPage(mainInteractor.getFavoritePageId())
    }

    override fun onAttach(view: FavoriteListView) {
        view.selectTab(mainInteractor.getFavoritePageId())

        recordsInteractor.recordsObs
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.showTabs(it.isNotEmpty()) })
                .addTo(viewSubs)
    }

    fun selectTab(position: Int) {
        mainInteractor.saveFavoritePageId(position)
        view?.showPage(position)
    }

}


