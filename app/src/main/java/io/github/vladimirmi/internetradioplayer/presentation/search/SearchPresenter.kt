package io.github.vladimirmi.internetradioplayer.presentation.search

import io.github.vladimirmi.internetradioplayer.domain.interactor.MainInteractor
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 20.03.2019.
 */

class SearchPresenter
@Inject constructor(private val mainInteractor: MainInteractor) : BasePresenter<SearchView>() {

    override fun onFirstAttach(view: SearchView) {
        view.showPage(mainInteractor.getFavoritePageId())
    }

    override fun onAttach(view: SearchView) {
        view.selectTab(mainInteractor.getFavoritePageId())
    }

    fun selectTab(position: Int) {
        mainInteractor.saveFavoritePageId(position)
        view?.showPage(position)
    }
}