package io.github.vladimirmi.radius.presentation.iconpicker

import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

/**
 * Created by Vladimir Mikhalev 15.12.2017.
 */

interface IconPickerView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setIconImage(icon: Bitmap)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setIconText(text: String)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setForegroundColor(colorInt: Int)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setBackgroundColor(colorInt: Int)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setOption(iconOption: IconOption)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setIconRes(iconRes: IconRes)
}