package io.github.vladimirmi.internetradioplayer.presentation.main

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.interactor.MainInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.MediaInteractor
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

class MainPresenter
@Inject constructor(private val router: Router,
                    private val mainInteractor: MainInteractor,
                    private val mediaInteractor: MediaInteractor)
    : BasePresenter<MainView>() {

    override fun onAttach(view: MainView) {
        mediaInteractor.currentMediaObs
                .map { !it.isNull() }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeX(onNext = { view.showPlayerView(it) })
                .addTo(viewSubs)
    }

    fun selectPage(position: Int) {
        val pageId = when (position) {
            0 -> R.id.nav_search
            1 -> R.id.nav_favorites
            2 -> R.id.nav_player
            else -> R.id.nav_history
        }
        mainInteractor.saveMainPageId(pageId)
        router.replaceScreen(pageId)
    }
}
