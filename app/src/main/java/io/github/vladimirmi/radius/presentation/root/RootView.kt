package io.github.vladimirmi.radius.presentation.root

import com.arellomobile.mvp.MvpView

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

interface RootView : MvpView {

    fun showToast(resId: Int)

    fun showSnackbar(resId: Int)

    fun showControls(visible: Boolean)
}