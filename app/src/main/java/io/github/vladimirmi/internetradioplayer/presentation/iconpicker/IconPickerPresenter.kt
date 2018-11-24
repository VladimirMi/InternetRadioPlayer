package io.github.vladimirmi.internetradioplayer.presentation.iconpicker

import io.github.vladimirmi.internetradioplayer.data.db.entity.Icon
import io.github.vladimirmi.internetradioplayer.domain.interactor.StationInteractor
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import io.github.vladimirmi.internetradioplayer.presentation.base.ToolbarBuilder
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 15.12.2017.
 */

class IconPickerPresenter
@Inject constructor(private val interactor: StationInteractor,
                    private val router: Router)
    : BasePresenter<IconPickerView>() {

    var currentIcon = Icon.randomIcon()

    override fun onFirstAttach(view: IconPickerView) {
        view.buildToolbar(ToolbarBuilder.standard()
                .setToolbarTitle(interactor.currentStation.name)
                .enableBackNavigation())
    }

    override fun onAttach(view: IconPickerView) {
        view.showControls(false)
    }

    fun saveIcon() {
//        interactor.currentStation = interactor.currentStation.copy(icon = currentIcon)
        exit()
    }

    fun onBackPressed(): Boolean {
        exit()
        return true
    }

    fun exit() {
        view?.showControls(true)
        router.exit()
    }
}
