package io.github.vladimirmi.radius.presentation.root

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

/**
 * Created by Vladimir Mikhalev 01.10.2017.
 */

interface RootView : MvpView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showToast(resId: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showSnackbar(resId: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showControls(visible: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showMetadata(visible: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showLoadingIndicator(visible: Boolean)
}