package io.github.vladimirmi.internetradioplayer.presentation.base

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

/**
 * Created by Vladimir Mikhalev 10.11.2018.
 */

interface BaseView {

    fun onBackPressed(): Boolean

    fun buildToolbar(builder: ToolbarBuilder)

    fun showToast(resId: Int)
}
