package io.github.vladimirmi.internetradioplayer.presentation.station

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import io.github.vladimirmi.internetradioplayer.data.db.entity.Group
import io.github.vladimirmi.internetradioplayer.data.db.entity.Station
import io.github.vladimirmi.internetradioplayer.presentation.base.ToolbarBuilder

/**
 * Created by Vladimir Mikhalev 18.11.2017.
 */

interface StationView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setStation(station: Station)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setGroup(group: Group)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setGenres(genres: List<String>)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun buildToolbar(builder: ToolbarBuilder)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setEditMode(editMode: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun editStation()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun createStation()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun openRemoveDialog()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun openLinkDialog(url: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun openCancelEditDialog()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun openCancelCreateDialog()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun openAddShortcutDialog()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showToast(resId: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun cancelEdit()
}
