package io.github.vladimirmi.internetradioplayer.presentation.base

import android.support.annotation.StringRes
import android.support.v7.app.AppCompatActivity
import android.view.Menu

/**
 * Created by Vladimir Mikhalev 29.11.2017.
 */

interface ToolbarView {

    fun setToolbarVisible(visible: Boolean)

    fun setToolbarTitle(@StringRes titleId: Int)

    fun setToolbarTitle(title: String)

    fun enableBackNavigation(backNavEnabled: Boolean)

    fun setMenu(menuHolder: MenuHolder)

    fun prepareOptionsMenu(menu: Menu)

    fun dismissToolbarMenu()

    fun setToolbarHost(activity: AppCompatActivity)
}
