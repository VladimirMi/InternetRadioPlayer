package io.github.vladimirmi.radius.ui.base

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView

/**
 * Created by Vladimir Mikhalev 28.10.2017.
 */

abstract class BasePresenter<V : MvpView> : MvpPresenter<V>(), LifecycleOwner {

    private var lifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun onFirstViewAttach() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        onFirstAttach()
    }

    abstract fun onFirstAttach()

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}