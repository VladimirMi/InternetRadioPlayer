package io.github.vladimirmi.internetradioplayer.presentation.base

import android.view.Menu
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity

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
