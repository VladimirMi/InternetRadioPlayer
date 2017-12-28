package io.github.vladimirmi.radius.presentation.root

import android.net.Uri
import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.extensions.ioToMain
import io.github.vladimirmi.radius.model.interactor.StationInteractor
import io.github.vladimirmi.radius.model.repository.MediaController
import io.github.vladimirmi.radius.navigation.Router
import io.github.vladimirmi.radius.ui.base.BasePresenter
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

@InjectViewState
class RootPresenter
@Inject constructor(private val router: Router,
                    private val mediaController: MediaController,
                    private val stationInteractor: StationInteractor)
    : BasePresenter<RootView>() {

    override fun onFirstViewAttach() {
        mediaController.connect()
        if (stationInteractor.hasStations()) {
            router.newRootScreen(Router.MEDIA_LIST_SCREEN)
        } else {
            router.newRootScreen(Router.GET_STARTED_SCREEN)
        }
    }

    override fun onDestroy() {
        mediaController.disconnect()
    }


    fun addStation(uri: Uri) {
        Timber.e("addStation: $uri")
        stationInteractor.createStation(uri)
                .ioToMain()
                .subscribeBy(
                        onSuccess = {
                            router.showStation(stationInteractor.currentStation)
                            viewState.showControls(true)
                        },
                        onError = {
                            Timber.e(it)
                            viewState.showToast(R.string.toast_add_error)
                        }
                ).addTo(compDisp)
    }
}