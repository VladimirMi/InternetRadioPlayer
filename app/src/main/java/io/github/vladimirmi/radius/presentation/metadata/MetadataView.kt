package io.github.vladimirmi.radius.presentation.metadata

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

/**
 * Created by Vladimir Mikhalev 08.12.2017.
 */

interface MetadataView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setInfo(string: String)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun hide()
}