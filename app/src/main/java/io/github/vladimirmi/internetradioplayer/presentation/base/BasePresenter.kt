package io.github.vladimirmi.internetradioplayer.presentation.base

import io.reactivex.disposables.CompositeDisposable

/**
 * Created by Vladimir Mikhalev 10.11.2018.
 */

open class BasePresenter<V : BaseView> {

    protected var view: V? = null
    protected val viewSubs = CompositeDisposable()
    protected val dataSubs = CompositeDisposable()
    private var isFirstAttach = true

    fun attachView(view: V) {
        this.view = view
        if (isFirstAttach) {
            onFirstAttach(view)
            isFirstAttach = false
        }
        onAttach(view)
    }

    fun detachView() {
        onDetach()
        viewSubs.clear()
        view = null
    }

    fun destroy() {
        onDestroy()
        isFirstAttach = true
        dataSubs.clear()
    }

    protected open fun onFirstAttach(view: V) {}

    protected open fun onAttach(view: V) {}

    protected open fun onDetach() {}

    protected open fun onDestroy() {}

    protected fun hasView(): Boolean {
        return view != null
    }
}
