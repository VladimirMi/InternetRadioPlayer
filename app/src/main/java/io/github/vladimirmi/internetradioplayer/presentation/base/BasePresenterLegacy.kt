package io.github.vladimirmi.internetradioplayer.presentation.base

import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by Vladimir Mikhalev 28.10.2017.
 */

abstract class BasePresenterLegacy<V : MvpView> : MvpPresenter<V>() {

    protected val subs = CompositeDisposable()

    override fun onDestroy() {
        subs.dispose()
    }
}
