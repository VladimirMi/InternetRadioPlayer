package io.github.vladimirmi.internetradioplayer.presentation.root

import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

interface RootView : BaseView {

    fun checkIntent()

    fun showControls(visible: Boolean)

    fun showLoadingIndicator(visible: Boolean)
}
