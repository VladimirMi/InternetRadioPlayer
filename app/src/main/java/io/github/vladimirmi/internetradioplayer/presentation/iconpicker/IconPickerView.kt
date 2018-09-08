package io.github.vladimirmi.internetradioplayer.presentation.iconpicker

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import io.github.vladimirmi.internetradioplayer.model.db.entity.Icon
import io.github.vladimirmi.internetradioplayer.presentation.root.ToolbarBuilder

/**
 * Created by Vladimir Mikhalev 15.12.2017.
 */

interface IconPickerView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun buildToolbar(builder: ToolbarBuilder)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setIcon(icon: Icon)
}
