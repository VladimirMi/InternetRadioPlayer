package io.github.vladimirmi.internetradioplayer.presentation.root

import android.support.annotation.StringRes

/**
 * Created by Vladimir Mikhalev 29.11.2017.
 */

interface ToolbarView {

    fun setToolbarVisible(visible: Boolean)

    fun setToolbarTitle(@StringRes titleId: Int)

    fun setToolbarTitle(title: String)

    fun enableBackNavigation(backNavEnabled: Boolean)

    fun setMenu(menuHolder: MenuHolder)
}