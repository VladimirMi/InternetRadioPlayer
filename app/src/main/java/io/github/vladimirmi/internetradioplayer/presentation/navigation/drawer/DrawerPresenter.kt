package io.github.vladimirmi.internetradioplayer.presentation.navigation.drawer

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.RecordsInteractor
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.github.vladimirmi.internetradioplayer.presentation.navigation.Router
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 08.04.2019.
 */

class DrawerPresenter
@Inject constructor(private val router: Router,
                    private val playerInteractor: PlayerInteractor,
                    private val recordsInteractor: RecordsInteractor)
    : BasePresenter<DrawerView>() {

    fun navigateTo(navId: Int) {
        when (navId) {
            R.id.nav_exit -> exitApp()
            R.id.nav_settings -> router.navigateTo(navId)
            else -> router.replaceScreen(navId)
        }
    }

    private fun exitApp() {
        playerInteractor.stop()
        recordsInteractor.stopAllRecordings()
        router.finishChain()
    }
}