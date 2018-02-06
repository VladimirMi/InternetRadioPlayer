package io.github.vladimirmi.internetradioplayer.ui.base

import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by Vladimir Mikhalev 28.10.2017.
 */

abstract class BasePresenter<V : MvpView> : MvpPresenter<V>() {

    protected val compDisp = CompositeDisposable()

    override fun onDestroy() {
        compDisp.dispose()
    }
}