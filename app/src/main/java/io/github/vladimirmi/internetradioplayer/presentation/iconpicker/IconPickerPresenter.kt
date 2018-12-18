package io.github.vladimirmi.internetradioplayer.presentation.iconpicker

import io.github.vladimirmi.internetradioplayer.domain.model.Icon
import io.github.vladimirmi.internetradioplayer.navigation.Router
import io.github.vladimirmi.internetradioplayer.presentation.base.BasePresenter
import javax.inject.Inject

/**
 * Created by Vladimir Mikhalev 15.12.2017.
 */

class IconPickerPresenter
@Inject constructor(private val router: Router)
    : BasePresenter<IconPickerView>() {

    var currentIcon = Icon.randomIcon(0)

    override fun onAttach(view: IconPickerView) {
        view.showControls(false)
    }

    fun saveIcon() {
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
