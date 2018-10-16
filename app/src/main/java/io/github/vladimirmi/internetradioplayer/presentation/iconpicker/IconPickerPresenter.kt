package io.github.vladimirmi.internetradioplayer.presentation.iconpicker

import com.arellomobile.mvp.InjectViewState
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.github.vladimirmi.internetradioplayer.presentation.base.ToolbarBuilder
import io.github.vladimirmi.internetradioplayer.presentation.root.RootPresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 15.12.2017.
 */

@InjectViewState
class IconPickerPresenter
@Inject constructor(private val rootPresenter: RootPresenter,
                    private val interactor: StationInteractor,
                    private val router: Router)
    : BasePresenter<IconPickerView>() {

    var currentIcon = interactor.currentStation.icon

    override fun onFirstViewAttach() {
        viewState.buildToolbar(ToolbarBuilder.standard()
                .setToolbarTitle(interactor.currentStation.name)
                .enableBackNavigation())
    }

    override fun attachView(view: IconPickerView?) {
        super.attachView(view)
        rootPresenter.viewState.showControls(false)
    }

    fun saveIcon() {
        interactor.currentStation = interactor.currentStation.copy(icon = currentIcon)
        exit()
    }

    fun onBackPressed(): Boolean {
        exit()
        return true
    }

    fun exit() {
        rootPresenter.viewState.showControls(true)
        router.exit()
    }
}
