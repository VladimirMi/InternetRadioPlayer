package io.github.vladimirmi.internetradioplayer.presentation.base

import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import io.github.vladimirmi.internetradioplayer.R
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by Vladimir Mikhalev 28.10.2017.
 */

abstract class BasePresenter<V : MvpView> : MvpPresenter<V>() {

    protected val subs = CompositeDisposable()

    override fun onDestroy() {
        subs.dispose()
    }

    protected fun getStandardToolbarBuilder(): ToolbarBuilder {
        val settingsItem = MenuItemHolder(R.string.menu_settings, R.drawable.ic_settings, order = 99)
        val exitItem = MenuItemHolder(R.string.menu_exit, R.drawable.ic_exit, order = 100)
        return ToolbarBuilder().apply {
            addMenuItem(settingsItem)
            addMenuItem(exitItem)
        }
    }
}
