package io.github.vladimirmi.internetradioplayer.presentation.player

import io.github.vladimirmi.internetradioplayer.R
import io.github.vladimirmi.internetradioplayer.domain.interactor.PlayerInteractor
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

class PlayerPresenter
@Inject constructor(private val stationInteractor: StationInteractor,
                    private val playerInteractor: PlayerInteractor,
                    private val router: Router)
    : BasePresenter<PlayerView>() {

    override fun onAttach(view: PlayerView) {
        view.setStation(stationInteractor.station)
    }

    fun removeStation() {
//        stationInteractor.removeStation(stationInteractor.currentStation.id)
//                .ioToMain()
//                .subscribeX(onComplete = {
//                    playerInteractor.stop()
//                    Timber.e("removeStation: ")
////                    if (interactor.haveStations()) router.exit()
////                    else router.newRootScreen(Router.GET_STARTED_SCREEN)
//                })
//                .addTo(dataSubs)
    }

    fun edit(stationInfo: StationInfo) {
//        stationInteractor.updateCurrentStation(stationInfo.stationName, stationInfo.groupName)
//                .ioToMain()
//                .subscribeX(onComplete = {})
//                .addTo(viewSubs)
    }

    fun cancelEdit() {
//        stationInteractor.currentStation = stationInteractor.previousWhenEdit!!
//        view?.setStation(stationInteractor.currentStation)
    }

    fun create(stationInfo: StationInfo) {
//        stationInteractor.addCurrentStation(stationInfo.stationName, stationInfo.groupName)
//                .ioToMain()
//                .subscribeX(
//                        onComplete = {
//                            view?.showMessage(R.string.msg_add_success)
//                            Timber.e("create: ${stationInfo.stationName}")
////                            router.newRootScreen(Router.STATIONS_LIST_SCREEN)
//                        })
//                .addTo(viewSubs)
    }

    fun cancelCreate() {
//        stationInteractor.currentStation = stationInteractor.previousWhenEdit!!
//        router.exit()
    }

    fun addShortcut(startPlay: Boolean) {
        if (stationInteractor.addCurrentShortcut(startPlay)) {
            view?.showMessage(R.string.msg_add_shortcut_success)
        }
    }
}
