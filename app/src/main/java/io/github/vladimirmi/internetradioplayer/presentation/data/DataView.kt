package io.github.vladimirmi.internetradioplayer.presentation.data

import io.github.vladimirmi.internetradioplayer.domain.model.Data
import io.github.vladimirmi.internetradioplayer.presentation.base.BaseView

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

interface DataView : BaseView {

    fun setData(data: List<Data>)

    fun selectData(id: String)

    fun showLoading(loading: Boolean)
}