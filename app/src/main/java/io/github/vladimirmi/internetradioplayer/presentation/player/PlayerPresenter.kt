package io.github.vladimirmi.internetradioplayer.presentation.player

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerControlsInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.extensions.ioToMain
import io.github.vladimirmi.internetradioplayer.extensions.subscribeX
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

class PlayerPresenter
@Inject constructor(private val interactor: StationInteractor,
                    private val controlsInteractor: PlayerControlsInteractor,
                    private val router: Router)
    : BasePresenter<PlayerView>() {

    override fun onAttach(view: PlayerView) {
        view.setStation(interactor.currentStation)
    }

    fun removeStation() {
        interactor.removeStation(interactor.currentStation.id)
                .ioToMain()
                .subscribeX(onComplete = {
                    controlsInteractor.stop()
                    Timber.e("removeStation: ")
//                    if (interactor.haveStations()) router.exit()
//                    else router.newRootScreen(Router.GET_STARTED_SCREEN)
                })
                .addTo(dataSubs)
    }

    fun edit(stationInfo: StationInfo) {
        interactor.updateCurrentStation(stationInfo.stationName, stationInfo.groupName)
                .ioToMain()
                .subscribeX(onComplete = {})
                .addTo(viewSubs)
    }

    fun cancelEdit() {
        interactor.currentStation = interactor.previousWhenEdit!!
        view?.setStation(interactor.currentStation)
    }

    fun create(stationInfo: StationInfo) {
        interactor.addCurrentStation(stationInfo.stationName, stationInfo.groupName)
                .ioToMain()
                .subscribeX(
                        onComplete = {
                            view?.showMessage(R.string.msg_add_success)
                            Timber.e("create: ${stationInfo.stationName}")
//                            router.newRootScreen(Router.STATIONS_LIST_SCREEN)
                        })
                .addTo(viewSubs)
    }

    fun cancelCreate() {
        interactor.currentStation = interactor.previousWhenEdit!!
        router.exit()
    }

    fun addShortcut(startPlay: Boolean) {
        if (interactor.addCurrentShortcut(startPlay)) {
            view?.showMessage(R.string.msg_add_shortcut_success)
        }
    }
}
