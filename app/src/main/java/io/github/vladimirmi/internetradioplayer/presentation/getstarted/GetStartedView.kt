package io.github.vladimirmi.internetradioplayer.presentation.getstarted

import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 23.12.2017.
 */

interface GetStartedView : BaseView {

    fun showControls(visible: Boolean)

    fun openAddStationDialog()
}
