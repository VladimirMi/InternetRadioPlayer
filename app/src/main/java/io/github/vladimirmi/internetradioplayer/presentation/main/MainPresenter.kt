package io.github.vladimirmi.internetradioplayer.presentation.main

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.interactor.MainInteractor
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

class MainPresenter
@Inject constructor(private val router: Router,
                    private val mainInteractor: MainInteractor)
    : BasePresenter<MainView>() {

    override fun onFirstAttach(view: MainView) {

    }

    fun selectPage(position: Int) {
        val pageId = when (position) {
            0 -> R.id.nav_search
            1 -> R.id.nav_stations
            else -> R.id.nav_player
        }
        mainInteractor.saveMainPageId(pageId)
        router.replaceScreen(pageId)
    }
}
