package io.github.vladimirmi.internetradioplayer.presentation.base

/**
 * Created by Vladimir Mikhalev 10.11.2018.
 */

interface BaseView : BackPressListener {

    fun showToast(resId: Int)

    fun showSnackbar(resId: Int)
}
