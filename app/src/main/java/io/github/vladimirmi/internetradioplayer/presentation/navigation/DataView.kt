package io.github.vladimirmi.internetradioplayer.presentation.navigation

import io.github.vladimirmi.internetradioplayer.data.net.model.StationSearchRes
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

interface DataView : BaseView {

    fun setData(data: List<StationSearchRes>)

    fun selectData(uri: String)

    fun showLoading(show: Boolean)
}